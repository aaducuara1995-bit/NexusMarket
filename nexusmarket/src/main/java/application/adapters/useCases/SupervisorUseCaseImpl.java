package application.adapters.useCases;

import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.BusinessProcess;
import application.domain.ports.in.SupervisorPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.AuthorizationService;

import java.util.List;

/** Read-only follow-up of the SUPERVISOR role (OBJ-12). Nothing here changes data. */
public class SupervisorUseCaseImpl implements SupervisorPort {

    private final AuthorizationService authorizationService;
    private final UserRepositoryPort userRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final OrderRepositoryPort orderRepository;

    public SupervisorUseCaseImpl(AuthorizationService authorizationService,
                                 UserRepositoryPort userRepository,
                                 WarehouseRepositoryPort warehouseRepository,
                                 InventoryRepositoryPort inventoryRepository,
                                 OrderRepositoryPort orderRepository) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<User> consultUsers(User supervisor) {
        requireReports(supervisor);
        return userRepository.findAll();
    }

    @Override
    public List<Warehouse> consultWarehouses(User supervisor) {
        requireReports(supervisor);
        return warehouseRepository.findAll();
    }

    @Override
    public List<Inventory> consultInventory(User supervisor) {
        requireReports(supervisor);
        return inventoryRepository.findAll();
    }

    @Override
    public List<Order> consultOrders(User supervisor) {
        requireReports(supervisor);
        return orderRepository.findAll();
    }

    private void requireReports(User user) {
        authorizationService.requirePermission(user, BusinessProcess.ADMINISTRATIVE_REPORTS);
    }
}