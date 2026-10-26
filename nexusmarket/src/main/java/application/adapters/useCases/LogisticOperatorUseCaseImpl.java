package application.adapters.useCases;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.BusinessProcess;
import application.domain.enums.OrderStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.in.LogisticOperatorPort;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.InventoryReservationRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.AuthorizationService;
import application.domain.services.ConfirmDeliveryService;
import application.domain.services.ShipOrderService;

import java.util.List;

/** Operations of the LOGISTIC_OPERATOR role: warehouse stock and order shipping. */
public class LogisticOperatorUseCaseImpl implements LogisticOperatorPort {

    private final AuthorizationService authorizationService;
    private final ShipOrderService shipOrderService;
    private final ConfirmDeliveryService confirmDeliveryService;
    private final InventoryOperationsSupport inventoryOperations;
    private final OrderRepositoryPort orderRepository;
    private final ShipmentRepositoryPort shipmentRepository;
    private final InventoryReservationRepositoryPort reservationRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final InventoryRepositoryPort inventoryRepository;

    public LogisticOperatorUseCaseImpl(AuthorizationService authorizationService,
                                       ShipOrderService shipOrderService,
                                       ConfirmDeliveryService confirmDeliveryService,
                                       InventoryOperationsSupport inventoryOperations,
                                       OrderRepositoryPort orderRepository,
                                       ShipmentRepositoryPort shipmentRepository,
                                       InventoryReservationRepositoryPort reservationRepository,
                                       InventoryMovementRepositoryPort movementRepository,
                                       InventoryRepositoryPort inventoryRepository) {
        this.authorizationService = authorizationService;
        this.shipOrderService = shipOrderService;
        this.confirmDeliveryService = confirmDeliveryService;
        this.inventoryOperations = inventoryOperations;
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.reservationRepository = reservationRepository;
        this.movementRepository = movementRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity) {
        requireOperator(user);
        return inventoryOperations.registerInitialInventory(user, null, product, warehouse, quantity);
    }

    @Override
    public Inventory adjustInventory(User user, Inventory inventory, int difference, String reason) {
        requireOperator(user);
        return inventoryOperations.adjust(user, null, inventory, difference, reason);
    }

    @Override
    public Inventory markInventoryDamaged(User user, Inventory inventory) {
        requireOperator(user);
        return inventoryOperations.markDamaged(user, null, inventory);
    }

    @Override
    public Inventory blockInventory(User user, Inventory inventory) {
        requireOperator(user);
        return inventoryOperations.block(user, null, inventory);
    }

    @Override
    public Inventory enableInventory(User user, Inventory inventory) {
        requireOperator(user);
        return inventoryOperations.enable(user, null, inventory);
    }

    @Override
    public Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units) {
        requireOperator(user);
        return inventoryOperations.restockFromReturn(user, null, inventory, returnRequest, units);
    }

    /** Paid orders with physical products are the ones waiting in the warehouse. */
    @Override
    public List<Order> consultOrdersToShip(User user) {
        authorizationService.requirePermission(user, BusinessProcess.SHIPPING_OPERATIONS);
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .filter(Order::containsPhysicalProducts)
                .toList();
    }

    @Override
    public Order shipOrder(User user, Order order) {
        authorizationService.requirePermission(user, BusinessProcess.SHIPPING_OPERATIONS);
        Order stored = Lookup.reload(order, Order::getId, orderRepository::findById, "Order");
        Shipment shipment = shipmentOf(stored);
        List<InventoryReservation> reservations = reservationRepository.findByOrder(stored.getId());

        List<InventoryMovement> movements = shipOrderService.ship(user, stored, shipment, reservations);

        List<Inventory> touched = reservations.stream()
                .map(InventoryReservation::getInventory)
                .distinct()
                .toList();
        inventoryRepository.saveAll(touched);
        reservationRepository.saveAll(reservations);
        movementRepository.saveAll(movements);
        shipmentRepository.save(shipment);
        orderRepository.save(stored);
        return stored;
    }

    @Override
    public Order confirmDelivery(User user, Order order) {
        authorizationService.requirePermission(user, BusinessProcess.SHIPPING_OPERATIONS);
        Order stored = Lookup.reload(order, Order::getId, orderRepository::findById, "Order");
        Shipment shipment = shipmentOf(stored);
        confirmDeliveryService.confirm(user, stored, shipment);
        shipmentRepository.save(shipment);
        orderRepository.save(stored);
        return stored;
    }

    private void requireOperator(User user) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (!user.hasRole(UserRole.LOGISTIC_OPERATOR))
            throw new AccessDeniedException("The user does not have the logistic operator role.");
    }

    private Shipment shipmentOf(Order order) {
        return shipmentRepository.findByOrder(order.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shipment"));
    }
}