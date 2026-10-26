package application.adapters.useCases;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.Invoice;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.InventoryReservationRepositoryPort;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.AddToCartService;
import application.domain.services.AuthorizationService;
import application.domain.services.ConfirmOrderService;
import application.domain.services.CreateShipmentService;
import application.domain.services.DeliverDigitalOrderService;
import application.domain.services.IssueInvoiceService;
import application.domain.services.PayOrderService;
import application.domain.services.RequestReturnService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;

import java.util.List;
import java.util.Optional;

/** Operations of the BUYER role: cart, order, payment and returns. */
public class BuyerUseCaseImpl implements BuyerPort {

    private final AuthorizationService authorizationService;
    private final AddToCartService addToCartService;
    private final ConfirmOrderService confirmOrderService;
    private final PayOrderService payOrderService;
    private final IssueInvoiceService issueInvoiceService;
    private final DeliverDigitalOrderService deliverDigitalOrderService;
    private final CreateShipmentService createShipmentService;
    private final RequestReturnService requestReturnService;
    private final BuyerRepositoryPort buyerRepository;
    private final CartRepositoryPort cartRepository;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final InventoryReservationRepositoryPort reservationRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final OrderRepositoryPort orderRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final ShipmentRepositoryPort shipmentRepository;
    private final ReturnRequestRepositoryPort returnRepository;

    public BuyerUseCaseImpl(AuthorizationService authorizationService,
                            AddToCartService addToCartService,
                            ConfirmOrderService confirmOrderService,
                            PayOrderService payOrderService,
                            IssueInvoiceService issueInvoiceService,
                            DeliverDigitalOrderService deliverDigitalOrderService,
                            CreateShipmentService createShipmentService,
                            RequestReturnService requestReturnService,
                            BuyerRepositoryPort buyerRepository,
                            CartRepositoryPort cartRepository,
                            ProductRepositoryPort productRepository,
                            InventoryRepositoryPort inventoryRepository,
                            InventoryReservationRepositoryPort reservationRepository,
                            InventoryMovementRepositoryPort movementRepository,
                            OrderRepositoryPort orderRepository,
                            InvoiceRepositoryPort invoiceRepository,
                            ShipmentRepositoryPort shipmentRepository,
                            ReturnRequestRepositoryPort returnRepository) {
        this.authorizationService = authorizationService;
        this.addToCartService = addToCartService;
        this.confirmOrderService = confirmOrderService;
        this.payOrderService = payOrderService;
        this.issueInvoiceService = issueInvoiceService;
        this.deliverDigitalOrderService = deliverDigitalOrderService;
        this.createShipmentService = createShipmentService;
        this.requestReturnService = requestReturnService;
        this.buyerRepository = buyerRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.movementRepository = movementRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.shipmentRepository = shipmentRepository;
        this.returnRepository = returnRepository;
    }

    @Override
    public Cart consultMyCart(User user) {
        authorizationService.requirePermission(user, BusinessProcess.ORDER_MANAGEMENT);
        return cartOf(buyerOf(user));
    }

    @Override
    public Cart addToCart(User user, Product product, int quantity) {
        Buyer buyer = buyerOf(user);
        Cart cart = cartOf(buyer);
        Product stored = Lookup.reload(product, Product::getId, productRepository::findById, "Product");
        addToCartService.add(user, buyer, cart, stored, quantity,
                inventoryRepository.findByProduct(stored.getId()));
        cartRepository.save(cart);
        return cart;
    }

    @Override
    public Cart removeFromCart(User user, Product product) {
        authorizationService.requirePermission(user, BusinessProcess.ORDER_MANAGEMENT);
        if (product == null)
            throw new IllegalArgumentException("Product is required.");
        Cart cart = cartOf(buyerOf(user));
        cart.removeProduct(product.getId());
        cartRepository.save(cart);
        return cart;
    }

    @Override
    public Order confirmOrder(User user, Address deliveryAddress) {
        Buyer buyer = buyerOf(user);
        Cart cart = cartOf(buyer);
        List<Inventory> inventories = cart.getItems().stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .flatMap(productId -> inventoryRepository.findByProduct(productId).stream())
                .toList();

        ConfirmOrderService.Result result =
                confirmOrderService.confirm(user, buyer, cart, inventories, deliveryAddress);

        orderRepository.save(result.order());
        reservationRepository.saveAll(result.reservations());
        movementRepository.saveAll(result.movements());
        inventoryRepository.saveAll(inventories);
        cartRepository.save(cart);
        return result.order();
    }

    /**
     * Pays the order and continues the flow agreed for NexusMarket: the invoice is issued
     * right away; a digital-only order is delivered at once and any other order gets a
     * shipment to the address chosen when it was confirmed.
     */
    @Override
    public Order payOrder(User user, Order order, Money paidAmount) {
        Buyer buyer = buyerOf(user);
        Order stored = Lookup.reload(order, Order::getId, orderRepository::findById, "Order");

        payOrderService.pay(user, buyer, stored, paidAmount);

        Invoice invoice = issueInvoiceService.issue(stored,
                listOf(invoiceRepository.findByOrder(stored.getId())));
        invoiceRepository.save(invoice);

        if (stored.isDigitalOnly()) {
            deliverDigitalOrderService.deliver(stored);
        } else {
            Shipment shipment = createShipmentService.create(stored, stored.getDeliveryAddress(),
                    listOf(shipmentRepository.findByOrder(stored.getId())));
            shipmentRepository.save(shipment);
        }

        orderRepository.save(stored);
        return stored;
    }

    @Override
    public List<Order> consultMyOrders(User user) {
        authorizationService.requirePermission(user, BusinessProcess.ORDER_MANAGEMENT);
        return orderRepository.findByBuyer(buyerOf(user).getId());
    }

    @Override
    public ReturnRequest requestReturn(User user, Order order, String reason) {
        Buyer buyer = buyerOf(user);
        Order stored = Lookup.reload(order, Order::getId, orderRepository::findById, "Order");
        ReturnRequest returnRequest = requestReturnService.request(user, buyer, stored, reason,
                returnRepository.findByOrder(stored.getId()));
        returnRepository.save(returnRequest);
        return returnRequest;
    }

    private Buyer buyerOf(User user) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (!user.hasRole(UserRole.BUYER))
            throw new AccessDeniedException("The user does not have the buyer role.");
        return buyerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer profile"));
    }

    private Cart cartOf(Buyer buyer) {
        return cartRepository.findByBuyer(buyer.getId()).orElseGet(() -> {
            Cart cart = new Cart(buyer);
            cartRepository.save(cart);
            return cart;
        });
    }

    private static <T> List<T> listOf(Optional<T> value) {
        return value.map(List::of).orElse(List.of());
    }
}