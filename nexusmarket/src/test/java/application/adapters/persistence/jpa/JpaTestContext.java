package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.repositories.*;
import application.adapters.useCases.AdminUseCaseImpl;
import application.adapters.useCases.BuyerUseCaseImpl;
import application.adapters.useCases.InventoryOperationsSupport;
import application.adapters.useCases.LogisticOperatorUseCaseImpl;
import application.adapters.useCases.PublicAccessUseCaseImpl;
import application.adapters.useCases.SellerUseCaseImpl;
import application.adapters.useCases.SupervisorUseCaseImpl;
import application.domain.services.*;

/**
 * The six use cases wired to the real JPA adapters. Only the Spring Data repositories
 * are replaced by in-memory fakes, so everything from the use case down to the
 * mappers runs as it will with MySQL.
 */
final class JpaTestContext {

    final SpringDataJpaUserRepository userRows = FakeSpringDataRepository.of(SpringDataJpaUserRepository.class);
    final SpringDataJpaBuyerRepository buyerRows = FakeSpringDataRepository.of(SpringDataJpaBuyerRepository.class);
    final SpringDataJpaSellerRepository sellerRows = FakeSpringDataRepository.of(SpringDataJpaSellerRepository.class);
    final SpringDataJpaWarehouseRepository warehouseRows = FakeSpringDataRepository.of(SpringDataJpaWarehouseRepository.class);
    final SpringDataJpaProductRepository productRows = FakeSpringDataRepository.of(SpringDataJpaProductRepository.class);
    final SpringDataJpaInventoryRepository inventoryRows = FakeSpringDataRepository.of(SpringDataJpaInventoryRepository.class);
    final SpringDataJpaInventoryReservationRepository reservationRows = FakeSpringDataRepository.of(SpringDataJpaInventoryReservationRepository.class);
    final SpringDataJpaInventoryMovementRepository movementRows = FakeSpringDataRepository.of(SpringDataJpaInventoryMovementRepository.class);
    final SpringDataJpaCartRepository cartRows = FakeSpringDataRepository.of(SpringDataJpaCartRepository.class);
    final SpringDataJpaOrderRepository orderRows = FakeSpringDataRepository.of(SpringDataJpaOrderRepository.class);
    final SpringDataJpaInvoiceRepository invoiceRows = FakeSpringDataRepository.of(SpringDataJpaInvoiceRepository.class);
    final SpringDataJpaShipmentRepository shipmentRows = FakeSpringDataRepository.of(SpringDataJpaShipmentRepository.class);
    final SpringDataJpaReturnRequestRepository returnRows = FakeSpringDataRepository.of(SpringDataJpaReturnRequestRepository.class);
    final SpringDataJpaRefundRepository refundRows = FakeSpringDataRepository.of(SpringDataJpaRefundRepository.class);

    final JpaReferenceResolver resolver =
            new JpaReferenceResolver(buyerRows, productRows, warehouseRows, inventoryRows, orderRows);

    final UserJpaAdapter users = new UserJpaAdapter(userRows);
    final BuyerJpaAdapter buyers = new BuyerJpaAdapter(buyerRows);
    final SellerJpaAdapter sellers = new SellerJpaAdapter(sellerRows);
    final WarehouseJpaAdapter warehouses = new WarehouseJpaAdapter(warehouseRows);
    final ProductJpaAdapter products = new ProductJpaAdapter(productRows);
    final InventoryJpaAdapter inventories = new InventoryJpaAdapter(inventoryRows, resolver);
    final InventoryReservationJpaAdapter reservations = new InventoryReservationJpaAdapter(reservationRows, resolver);
    final InventoryMovementJpaAdapter movements = new InventoryMovementJpaAdapter(movementRows);
    final CartJpaAdapter carts = new CartJpaAdapter(cartRows, resolver);
    final OrderJpaAdapter orders = new OrderJpaAdapter(orderRows, resolver);
    final InvoiceJpaAdapter invoices = new InvoiceJpaAdapter(invoiceRows, resolver);
    final ShipmentJpaAdapter shipments = new ShipmentJpaAdapter(shipmentRows, resolver);
    final ReturnRequestJpaAdapter returns = new ReturnRequestJpaAdapter(returnRows, resolver);
    final RefundJpaAdapter refunds = new RefundJpaAdapter(refundRows, resolver);
    final InMemoryRepositories.AuditLogs auditLogs = new InMemoryRepositories.AuditLogs();

    final PublicAccessUseCaseImpl publicAccess;
    final BuyerUseCaseImpl buyerUseCase;
    final SellerUseCaseImpl sellerUseCase;
    final LogisticOperatorUseCaseImpl logisticUseCase;
    final AdminUseCaseImpl adminUseCase;
    final SupervisorUseCaseImpl supervisorUseCase;

    JpaTestContext() {
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
    }
}