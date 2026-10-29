package application.adapters.rest;

import application.adapters.rest.controllers.AdminRestController;
import application.adapters.rest.controllers.PublicAccessRestController;
import application.adapters.rest.controllers.SupervisorRestController;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.useCases.AdminUseCaseImpl;
import application.adapters.useCases.BuyerUseCaseImpl;
import application.adapters.useCases.InventoryOperationsSupport;
import application.adapters.useCases.LogisticOperatorUseCaseImpl;
import application.adapters.useCases.PublicAccessUseCaseImpl;
import application.adapters.useCases.SellerUseCaseImpl;
import application.adapters.useCases.SupervisorUseCaseImpl;
import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.services.*;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The REST controllers wired to the real use cases and in-memory repositories, so the
 * tests call each endpoint method as plain Java and check status codes and bodies.
 */
public final class RestTestContext {

    public final InMemoryRepositories.Users users = new InMemoryRepositories.Users();
    public final InMemoryRepositories.Buyers buyers = new InMemoryRepositories.Buyers();
    public final InMemoryRepositories.Sellers sellers = new InMemoryRepositories.Sellers();
    public final InMemoryRepositories.Warehouses warehouses = new InMemoryRepositories.Warehouses();
    public final InMemoryRepositories.Products products = new InMemoryRepositories.Products();
    public final InMemoryRepositories.Inventories inventories = new InMemoryRepositories.Inventories();
    public final InMemoryRepositories.Reservations reservations = new InMemoryRepositories.Reservations();
    public final InMemoryRepositories.Movements movements = new InMemoryRepositories.Movements();
    public final InMemoryRepositories.Carts carts = new InMemoryRepositories.Carts();
    public final InMemoryRepositories.Orders orders = new InMemoryRepositories.Orders();
    public final InMemoryRepositories.Invoices invoices = new InMemoryRepositories.Invoices();
    public final InMemoryRepositories.Shipments shipments = new InMemoryRepositories.Shipments();
    public final InMemoryRepositories.Returns returns = new InMemoryRepositories.Returns();
    public final InMemoryRepositories.Refunds refunds = new InMemoryRepositories.Refunds();
    public final InMemoryRepositories.AuditLogs auditLogs = new InMemoryRepositories.AuditLogs();

    public final PublicAccessUseCaseImpl publicAccess;
    public final BuyerUseCaseImpl buyerUseCase;
    public final SellerUseCaseImpl sellerUseCase;
    public final LogisticOperatorUseCaseImpl logisticUseCase;
    public final AdminUseCaseImpl adminUseCase;
    public final SupervisorUseCaseImpl supervisorUseCase;

    public final AuthenticatedUserResolver currentUser = new AuthenticatedUserResolver(users);
    public final RestReferenceLoader loader = new RestReferenceLoader(users, sellers, warehouses, products,
            inventories, orders, returns, refunds);
    public final GlobalExceptionHandler errors = new GlobalExceptionHandler();

    public final PublicAccessRestController publicController;
    public final AdminRestController adminController;
    public final SupervisorRestController supervisorController;

    public final User admin;

    public RestTestContext() {
        AuthorizationService authorization = new AuthorizationService();
        BuyerValidationService buyerValidation = new BuyerValidationService();
        SellerValidationService sellerValidation = new SellerValidationService();
        InventoryAccessService inventoryAccess = new InventoryAccessService(authorization, sellerValidation);
        RegisterUserService registerUser = new RegisterUserService(authorization, users);
        RegisterAuditLogService audit = new RegisterAuditLogService(auditLogs);
        ConsultAuditLogService consultAudit = new ConsultAuditLogService(authorization, auditLogs);

        publicAccess = new PublicAccessUseCaseImpl(registerUser, new RegisterBuyerService(buyers),
                new CheckAvailabilityService(), products, inventories, audit);
        buyerUseCase = new BuyerUseCaseImpl(authorization, new AddToCartService(buyerValidation),
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
                new OnboardSellerService(authorization), new RegisterWarehouseService(authorization, warehouses),
                new ResolveReturnService(authorization), new GenerateRefundService(),
                new ProcessRefundService(authorization),
                users, sellers, warehouses, returns, refunds, audit, consultAudit);
        supervisorUseCase = new SupervisorUseCaseImpl(authorization, users, warehouses, inventories, orders, consultAudit);

        publicController = new PublicAccessRestController(publicAccess, loader);
        adminController = new AdminRestController(adminUseCase, currentUser, loader);
        supervisorController = new SupervisorRestController(supervisorUseCase, currentUser);

        admin = new User(new FullName("Administrador", "NexusMarket"), new Email("admin@nexusmarket.com"),
                new IdentityDocument("1000000000"), UserRole.ADMIN);
        users.save(admin);
    }

    /** A fake HTTP request for the exception handler tests. */
    public static HttpServletRequest request(String method, String path) {
        return (HttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> switch (m.getName()) {
                    case "getRequestURI" -> path;
                    case "getMethod" -> method;
                    default -> null;
                });
    }
}