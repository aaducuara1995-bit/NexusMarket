package application.adapters.useCases;

import application.domain.entities.Buyer;
import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseType;
import application.domain.services.*;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Wires the six use cases with real domain services and in-memory repositories,
 * the same way Spring will wire them with the MySQL adapters in phase B.
 */
final class UseCaseTestContext {

    final InMemoryRepositories.Users users = new InMemoryRepositories.Users();
    final InMemoryRepositories.Buyers buyers = new InMemoryRepositories.Buyers();
    final InMemoryRepositories.Sellers sellers = new InMemoryRepositories.Sellers();
    final InMemoryRepositories.Warehouses warehouses = new InMemoryRepositories.Warehouses();
    final InMemoryRepositories.Products products = new InMemoryRepositories.Products();
    final InMemoryRepositories.Inventories inventories = new InMemoryRepositories.Inventories();
    final InMemoryRepositories.Reservations reservations = new InMemoryRepositories.Reservations();
    final InMemoryRepositories.Movements movements = new InMemoryRepositories.Movements();
    final InMemoryRepositories.Carts carts = new InMemoryRepositories.Carts();
    final InMemoryRepositories.Orders orders = new InMemoryRepositories.Orders();
    final InMemoryRepositories.Invoices invoices = new InMemoryRepositories.Invoices();
    final InMemoryRepositories.Shipments shipments = new InMemoryRepositories.Shipments();
    final InMemoryRepositories.Returns returns = new InMemoryRepositories.Returns();
    final InMemoryRepositories.Refunds refunds = new InMemoryRepositories.Refunds();
    final InMemoryRepositories.AuditLogs auditLogs = new InMemoryRepositories.AuditLogs();

    final PublicAccessUseCaseImpl publicAccess;
    final BuyerUseCaseImpl buyerUseCase;
    final SellerUseCaseImpl sellerUseCase;
    final LogisticOperatorUseCaseImpl logisticUseCase;
    final AdminUseCaseImpl adminUseCase;
    final SupervisorUseCaseImpl supervisorUseCase;

    private int sequence = 0;

    UseCaseTestContext() {
        AuthorizationService authorization = new AuthorizationService();
        BuyerValidationService buyerValidation = new BuyerValidationService();
        SellerValidationService sellerValidation = new SellerValidationService();
        InventoryAccessService inventoryAccess = new InventoryAccessService(authorization, sellerValidation);
        RegisterUserService registerUser = new RegisterUserService(authorization, users, new InMemoryRepositories.FakePasswords());
        RegisterAuditLogService audit = new RegisterAuditLogService(auditLogs);
        ConsultAuditLogService consultAudit = new ConsultAuditLogService(authorization, auditLogs);

        publicAccess = new PublicAccessUseCaseImpl(new LoginService(users, new InMemoryRepositories.FakePasswords(), new InMemoryRepositories.FakeTokens()), registerUser, new RegisterBuyerService(buyers),
                new CheckAvailabilityService(), products, inventories, audit);

        buyerUseCase = new BuyerUseCaseImpl(authorization,
                new AddToCartService(buyerValidation),
                new ConfirmOrderService(buyerValidation, new ReserveInventoryService(), new ReleaseReservationService()),
                new PayOrderService(buyerValidation), new IssueInvoiceService(),
                new DeliverDigitalOrderService(), new CreateShipmentService(),
                new RequestReturnService(authorization),
                buyers, carts, products, inventories, reservations, movements,
                orders, invoices, shipments, returns, audit);

        InventoryOperationsSupport inventoryOperations = new InventoryOperationsSupport(
                new RegisterInitialInventoryService(inventoryAccess), new AdjustInventoryService(inventoryAccess),
                new MarkInventoryService(inventoryAccess), new RestockInventoryService(inventoryAccess),
                products, warehouses, inventories, movements, returns, audit);

        sellerUseCase = new SellerUseCaseImpl(authorization,
                new RegisterProductService(authorization, sellerValidation, products),
                new PublishProductService(authorization, sellerValidation),
                new ChangeProductStatusService(authorization, sellerValidation),
                inventoryOperations, sellers, products, inventories, audit);

        logisticUseCase = new LogisticOperatorUseCaseImpl(authorization,
                new ShipOrderService(authorization, new InventoryOutflowService()),
                new ConfirmDeliveryService(authorization), inventoryOperations,
                orders, shipments, reservations, movements, inventories, audit);

        adminUseCase = new AdminUseCaseImpl(authorization, registerUser,
                new OnboardSellerService(authorization),
                new RegisterWarehouseService(authorization, warehouses),
                new ResolveReturnService(authorization), new GenerateRefundService(),
                new ProcessRefundService(authorization),
                users, sellers, warehouses, returns, refunds, audit, consultAudit);

        supervisorUseCase = new SupervisorUseCaseImpl(authorization, users, warehouses, inventories, orders, consultAudit);
    }

    // ----- Builders for the objects a controller would send -----

    User newUser(UserRole role) {
        sequence++;
        return new User(new FullName("Person", "Number" + sequence),
                new Email("person" + sequence + "@mail.com"),
                new IdentityDocument("10" + sequence),
                role);
    }

    static Address address(String street) {
        return new Address(street, "Medellin", "Antioquia", "050001");
    }

    static Product draftProduct(String name, ProductType type) {
        return new Product(UUID.randomUUID(), name, "Description", type,
                new Money(new BigDecimal("45000")));
    }

    static Warehouse draftWarehouse(String name) {
        return Warehouse.ofMarketplace(name, WarehouseType.PHYSICAL, address("Bodega 1"));
    }

    // ----- Scenario steps built with the use cases themselves -----

    /** The first admin is the only record inserted directly; everything else goes through the use cases. */
    User admin() {
        User admin = newUser(UserRole.ADMIN);
        users.save(admin);
        return admin;
    }

    User registered(User admin, UserRole role) {
        return adminUseCase.registerUser(admin, newUser(role), "secret123");
    }

    /** A seller user with their profile and a first warehouse. */
    User sellerUser(User admin) {
        User sellerUser = registered(admin, UserRole.SELLER);
        adminUseCase.onboardSeller(admin, sellerUser, draftWarehouse("Warehouse " + sellerUser.getEmail().value()));
        return sellerUser;
    }

    Seller sellerOf(User sellerUser) {
        return sellers.findByUserId(sellerUser.getId()).orElseThrow();
    }

    Warehouse warehouseOf(User sellerUser) {
        UUID sellerId = sellerOf(sellerUser).getId();
        return warehouses.findAll().stream().filter(w -> w.belongsTo(sellerId)).findFirst().orElseThrow();
    }

    /** A published physical product with stock in the seller's warehouse. */
    Product physicalProduct(User sellerUser, int stock) {
        Product product = sellerUseCase.registerProduct(sellerUser, draftProduct("Shirt " + (++sequence), ProductType.PHYSICAL));
        sellerUseCase.registerInitialInventory(sellerUser, product, warehouseOf(sellerUser), stock);
        return sellerUseCase.publishProduct(sellerUser, product);
    }

    Product digitalProduct(User sellerUser) {
        Product product = sellerUseCase.registerProduct(sellerUser, draftProduct("Ebook " + (++sequence), ProductType.DIGITAL));
        return sellerUseCase.publishProduct(sellerUser, product);
    }

    User buyerUser() {
        User user = newUser(UserRole.BUYER);
        Buyer buyer = publicAccess.registerBuyer(user, address("Calle 10 # 20-30"), "secret123");
        return users.findById(buyer.getUserId()).orElseThrow();
    }

    Order paidOrder(User buyerUser, Product product, int quantity) {
        buyerUseCase.addToCart(buyerUser, product, quantity);
        Order order = buyerUseCase.confirmOrder(buyerUser, null);
        return buyerUseCase.payOrder(buyerUser, order, order.calculateTotal());
    }

    Inventory inventoryOf(Product product) {
        return inventories.findByProduct(product.getId()).get(0);
    }
}