# Fase B2a: auditoria de operaciones en el dominio (AuditLog). Ejecutar dentro de la carpeta nexusmarket.
if (-not (Test-Path "pom.xml") -or -not (Test-Path "src\main\java\application\domain\entities\Restore.java")) {
    Write-Host "Ejecuta este script dentro de la carpeta nexusmarket, despues de la Fase B1." -ForegroundColor Red
    exit 1
}
$sucio = git status --porcelain -- src ..\SDD 2>$null
if ($sucio) {
    Write-Host "Hay cambios sin subir en src o SDD. Haz commit y push antes de continuar." -ForegroundColor Red
    exit 1
}
$utf8 = New-Object System.Text.UTF8Encoding $false
$creados = 0
function Escribir($ruta, $contenido) {
    $completa = [System.IO.Path]::GetFullPath((Join-Path $PWD $ruta))
    New-Item -ItemType Directory -Force (Split-Path $completa -Parent) | Out-Null
    [System.IO.File]::WriteAllText($completa, $contenido, $utf8)
    Write-Host "Escrito: $ruta" -ForegroundColor Green
    $script:creados++
}

Escribir "src\test\java\application\domain\services\AuditLogServiceTest.java" @'
package application.domain.services;

import application.domain.entities.AuditLog;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogServiceTest {

    private final InMemoryRepositories.AuditLogs repository = new InMemoryRepositories.AuditLogs();
    private final RegisterAuditLogService register = new RegisterAuditLogService(repository);
    private final ConsultAuditLogService consult =
            new ConsultAuditLogService(new AuthorizationService(), repository);

    @Test
    void theEntryKeepsWhoDidWhatAndOnWhichRecord() {
        User seller = Fixtures.user(UserRole.SELLER);
        UUID productId = UUID.randomUUID();

        AuditLog entry = register.register(AuditOperation.PRODUCT_REGISTRATION, seller, productId,
                Map.of("name", "Mug"));

        assertEquals(seller.getId(), entry.getPerformedByUserId());
        assertEquals(UserRole.SELLER, entry.getPerformedByRole());
        assertEquals(productId, entry.getAffectedEntityId());
        assertEquals("Mug", entry.getDetails().get("name"));
        assertSame(entry, repository.saved.get(0));
    }

    @Test
    void theDetailsCannotBeChangedAfterwards() {
        Map<String, String> details = new HashMap<>(Map.of("status", "PAID"));
        AuditLog entry = new AuditLog(AuditOperation.ORDER_PAYMENT, Fixtures.user(UserRole.BUYER),
                UUID.randomUUID(), details);

        details.put("status", "CHANGED");

        assertEquals("PAID", entry.getDetails().get("status"));
        assertThrows(UnsupportedOperationException.class, () -> entry.getDetails().put("x", "y"));
    }

    @Test
    void anEntryNeedsOperationUserAndRecord() {
        User user = Fixtures.user(UserRole.ADMIN);
        assertThrows(IllegalArgumentException.class, () -> new AuditLog(null, user, UUID.randomUUID(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new AuditLog(AuditOperation.USER_BLOCKING, null, UUID.randomUUID(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new AuditLog(AuditOperation.USER_BLOCKING, user, null, null));
    }

    @Test
    void supervisorAndAdminReadTheLogNewestFirst() {
        User admin = Fixtures.user(UserRole.ADMIN);
        UUID orderId = UUID.randomUUID();
        AuditLog older = AuditLog.restore(UUID.randomUUID(), AuditOperation.ORDER_CONFIRMATION,
                LocalDateTime.now().minusHours(2), admin.getId(), UserRole.BUYER, orderId, Map.of());
        repository.save(older);
        AuditLog newer = register.register(AuditOperation.ORDER_PAYMENT, admin, orderId, null);
        register.register(AuditOperation.USER_BLOCKING, admin, UUID.randomUUID(), null);

        List<AuditLog> ofOrder = consult.consultByEntity(Fixtures.user(UserRole.SUPERVISOR), orderId);

        assertEquals(List.of(newer, older), ofOrder);
        assertEquals(3, consult.consultAll(admin).size());
    }

    @Test
    void otherRolesCannotReadTheLog() {
        assertThrows(AccessDeniedException.class, () -> consult.consultAll(Fixtures.user(UserRole.BUYER)));
        assertThrows(AccessDeniedException.class,
                () -> consult.consultAll(Fixtures.user(UserRole.LOGISTIC_OPERATOR)));
    }
}
'@

Escribir "src\test\java\application\domain\services\InMemoryRepositories.java" @'
package application.domain.services;

import application.domain.entities.AuditLog;
import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Invoice;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.InventoryReservationRepositoryPort;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/** Simple in-memory implementations of the output ports, for tests only. */
public final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    /** Keeps one copy per identifier: saving an existing object replaces it. */
    public abstract static class Store<T> {
        public final List<T> saved = new ArrayList<>();

        protected abstract UUID idOf(T item);

        public void save(T item) {
            saved.removeIf(existing -> idOf(existing).equals(idOf(item)));
            saved.add(item);
        }

        public void saveAll(List<T> items) {
            items.forEach(this::save);
        }

        public List<T> findAll() {
            return List.copyOf(saved);
        }

        public Optional<T> findById(UUID id) {
            return first(item -> idOf(item).equals(id));
        }

        protected Optional<T> first(Predicate<T> condition) {
            return saved.stream().filter(condition).findFirst();
        }

        protected List<T> where(Predicate<T> condition) {
            return saved.stream().filter(condition).toList();
        }
    }

    public static class Users extends Store<User> implements UserRepositoryPort {
        protected UUID idOf(User user) { return user.getId(); }

        public boolean existsByEmail(Email email) {
            return first(u -> u.getEmail().equals(email)).isPresent();
        }

        public boolean existsByDocument(IdentityDocument document) {
            return first(u -> u.getDocument().equals(document)).isPresent();
        }

        public Optional<User> findByEmail(Email email) {
            return first(u -> u.getEmail().equals(email));
        }
    }

    public static class Buyers extends Store<Buyer> implements BuyerRepositoryPort {
        protected UUID idOf(Buyer buyer) { return buyer.getId(); }

        public boolean existsByUserId(UUID userId) {
            return findByUserId(userId).isPresent();
        }

        public Optional<Buyer> findByUserId(UUID userId) {
            return first(b -> b.getUserId().equals(userId));
        }
    }

    public static class Sellers extends Store<Seller> implements SellerRepositoryPort {
        protected UUID idOf(Seller seller) { return seller.getId(); }

        public boolean existsByUserId(UUID userId) {
            return findByUserId(userId).isPresent();
        }

        public Optional<Seller> findByUserId(UUID userId) {
            return first(s -> s.getUserId().equals(userId));
        }
    }

    public static class Warehouses extends Store<Warehouse> implements WarehouseRepositoryPort {
        protected UUID idOf(Warehouse warehouse) { return warehouse.getId(); }

        public boolean existsByName(String name) {
            return first(w -> w.getName().equalsIgnoreCase(name)).isPresent();
        }
    }

    public static class Products extends Store<Product> implements ProductRepositoryPort {
        protected UUID idOf(Product product) { return product.getId(); }

        public boolean existsNameForSeller(UUID sellerId, String name) {
            return first(p -> p.getSellerId().equals(sellerId) && p.getName().equalsIgnoreCase(name)).isPresent();
        }

        public List<Product> findBySeller(UUID sellerId) {
            return where(p -> p.getSellerId().equals(sellerId));
        }

        public List<Product> findPublished() {
            return where(Product::isPublished);
        }
    }

    public static class Inventories extends Store<Inventory> implements InventoryRepositoryPort {
        protected UUID idOf(Inventory inventory) { return inventory.getId(); }

        public List<Inventory> findByProduct(UUID productId) {
            return where(i -> i.getProduct().getId().equals(productId));
        }
    }

    public static class Reservations extends Store<InventoryReservation> implements InventoryReservationRepositoryPort {
        protected UUID idOf(InventoryReservation reservation) { return reservation.getId(); }

        public List<InventoryReservation> findByOrder(UUID orderId) {
            return where(r -> r.belongsToOrder(orderId));
        }
    }

    public static class Movements extends Store<InventoryMovement> implements InventoryMovementRepositoryPort {
        protected UUID idOf(InventoryMovement movement) { return movement.getId(); }

        public List<InventoryMovement> findByInventory(UUID inventoryId) {
            return where(m -> m.getInventoryId().equals(inventoryId));
        }
    }

    public static class Carts extends Store<Cart> implements CartRepositoryPort {
        protected UUID idOf(Cart cart) { return cart.getId(); }

        public Optional<Cart> findByBuyer(UUID buyerId) {
            return first(c -> c.getBuyer().getId().equals(buyerId));
        }
    }

    public static class Orders extends Store<Order> implements OrderRepositoryPort {
        protected UUID idOf(Order order) { return order.getId(); }

        public List<Order> findByBuyer(UUID buyerId) {
            return where(o -> o.belongsTo(buyerId));
        }
    }

    public static class Invoices extends Store<Invoice> implements InvoiceRepositoryPort {
        protected UUID idOf(Invoice invoice) { return invoice.getId(); }

        public Optional<Invoice> findByOrder(UUID orderId) {
            return first(i -> i.getOrder().getId().equals(orderId));
        }
    }

    public static class Shipments extends Store<Shipment> implements ShipmentRepositoryPort {
        protected UUID idOf(Shipment shipment) { return shipment.getId(); }

        public Optional<Shipment> findByOrder(UUID orderId) {
            return first(s -> s.getOrder().getId().equals(orderId));
        }
    }

    public static class Returns extends Store<ReturnRequest> implements ReturnRequestRepositoryPort {
        protected UUID idOf(ReturnRequest returnRequest) { return returnRequest.getId(); }

        public List<ReturnRequest> findByOrder(UUID orderId) {
            return where(r -> r.getOrder().getId().equals(orderId));
        }
    }

    public static class Refunds extends Store<Refund> implements RefundRepositoryPort {
        protected UUID idOf(Refund refund) { return refund.getId(); }

        public Optional<Refund> findByOrder(UUID orderId) {
            return first(r -> r.getOrder().getId().equals(orderId));
        }
    }

    public static class AuditLogs extends Store<AuditLog> implements AuditLogRepositoryPort {
        protected UUID idOf(AuditLog auditLog) { return auditLog.getId(); }

        public List<AuditLog> findByUser(UUID userId) {
            return where(a -> a.getPerformedByUserId().equals(userId));
        }

        public List<AuditLog> findByAffectedEntity(UUID entityId) {
            return where(a -> a.getAffectedEntityId().equals(entityId));
        }
    }
}
'@

Escribir "src\test\java\application\adapters\useCases\AuditTrailUseCaseTest.java" @'
package application.adapters.useCases;

import application.domain.entities.AuditLog;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditTrailUseCaseTest {

    @Test
    void theWholeLifeOfAnOrderIsAudited() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        Product product = ctx.physicalProduct(ctx.sellerUser(admin), 10);
        User buyer = ctx.buyerUser();
        Order order = ctx.paidOrder(buyer, product, 2);
        User operator = ctx.registered(admin, UserRole.LOGISTIC_OPERATOR);
        ctx.logisticUseCase.shipOrder(operator, order);
        ctx.logisticUseCase.confirmDelivery(operator, order);
        User supervisor = ctx.registered(admin, UserRole.SUPERVISOR);

        List<AuditLog> ofOrder = ctx.supervisorUseCase.consultAuditLogOf(supervisor, order.getId());

        assertEquals(List.of(AuditOperation.ORDER_DELIVERY, AuditOperation.ORDER_SHIPMENT,
                        AuditOperation.ORDER_PAYMENT, AuditOperation.ORDER_CONFIRMATION),
                ofOrder.stream().map(AuditLog::getOperation).toList());
        assertEquals(buyer.getId(), ofOrder.get(3).getPerformedByUserId());
        assertEquals(UserRole.LOGISTIC_OPERATOR, ofOrder.get(0).getPerformedByRole());
    }

    @Test
    void administrativeOperationsAreAuditedToo() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        User seller = ctx.sellerUser(admin);
        ctx.adminUseCase.blockUser(admin, seller);

        List<AuditOperation> operations = ctx.adminUseCase.consultAuditLog(admin).stream()
                .map(AuditLog::getOperation).toList();

        assertTrue(operations.contains(AuditOperation.USER_REGISTRATION));
        assertTrue(operations.contains(AuditOperation.SELLER_ONBOARDING));
        assertTrue(operations.contains(AuditOperation.USER_BLOCKING));
    }

    @Test
    void aFailedOperationLeavesNoTrace() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User seller = ctx.sellerUser(ctx.admin());
        Product product = ctx.physicalProduct(seller, 3);
        int before = ctx.auditLogs.saved.size();

        assertThrows(IllegalStateException.class,
                () -> ctx.sellerUseCase.adjustInventory(seller, ctx.inventoryOf(product), -5, "Count"));

        assertEquals(before, ctx.auditLogs.saved.size());
    }

    @Test
    void onlySupervisorAndAdminReadTheTrail() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User buyer = ctx.buyerUser();

        assertThrows(AccessDeniedException.class, () -> ctx.supervisorUseCase.consultAuditLog(buyer));
    }
}
'@

Escribir "src\test\java\application\adapters\useCases\UseCaseTestContext.java" @'
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
        RegisterUserService registerUser = new RegisterUserService(authorization, users);
        RegisterAuditLogService audit = new RegisterAuditLogService(auditLogs);
        ConsultAuditLogService consultAudit = new ConsultAuditLogService(authorization, auditLogs);

        publicAccess = new PublicAccessUseCaseImpl(registerUser, new RegisterBuyerService(buyers),
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
        return adminUseCase.registerUser(admin, newUser(role));
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
        Buyer buyer = publicAccess.registerBuyer(user, address("Calle 10 # 20-30"));
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
'@

Escribir "src\main\java\application\domain\services\ConsultAuditLogService.java" @'
package application.domain.services;

import application.domain.entities.AuditLog;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.ports.out.AuditLogRepositoryPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Read-only access to the audit trail, reserved for administrative follow-up (OBJ-12). */
public class ConsultAuditLogService {

    private final AuthorizationService authorization;
    private final AuditLogRepositoryPort auditLogs;

    public ConsultAuditLogService(AuthorizationService authorization, AuditLogRepositoryPort auditLogs) {
        if (authorization == null || auditLogs == null)
            throw new IllegalArgumentException("Service dependencies are required.");
        this.authorization = authorization;
        this.auditLogs = auditLogs;
    }

    public List<AuditLog> consultAll(User actor) {
        authorization.requirePermission(actor, BusinessProcess.ADMINISTRATIVE_REPORTS);
        return newestFirst(auditLogs.findAll());
    }

    public List<AuditLog> consultByEntity(User actor, UUID entityId) {
        authorization.requirePermission(actor, BusinessProcess.ADMINISTRATIVE_REPORTS);
        if (entityId == null)
            throw new IllegalArgumentException("The record to consult is required.");
        return newestFirst(auditLogs.findByAffectedEntity(entityId));
    }

    /**
     * Two entries can share the same timestamp when they happen in the same instant, so
     * the list is reversed first: among equal dates, the one saved last comes first.
     */
    private static List<AuditLog> newestFirst(List<AuditLog> entries) {
        List<AuditLog> sorted = new ArrayList<>(entries);
        Collections.reverse(sorted);
        sorted.sort(Comparator.comparing(AuditLog::getDate).reversed());
        return List.copyOf(sorted);
    }
}
'@

Escribir "src\main\java\application\domain\services\RegisterAuditLogService.java" @'
package application.domain.services;

import application.domain.entities.AuditLog;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.ports.out.AuditLogRepositoryPort;

import java.util.Map;
import java.util.UUID;

/** Leaves the audit record of an operation that has already succeeded. */
public class RegisterAuditLogService {

    private final AuditLogRepositoryPort auditLogs;

    public RegisterAuditLogService(AuditLogRepositoryPort auditLogs) {
        if (auditLogs == null)
            throw new IllegalArgumentException("Audit log repository is required.");
        this.auditLogs = auditLogs;
    }

    public AuditLog register(AuditOperation operation, User performedBy,
                             UUID affectedEntityId, Map<String, String> details) {
        AuditLog auditLog = new AuditLog(operation, performedBy, affectedEntityId, details);
        auditLogs.save(auditLog);
        return auditLog;
    }
}
'@

Escribir "src\main\java\application\domain\entities\AuditLog.java" @'
package application.domain.entities;

import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable record of a business operation: who performed it, with which role, when,
 * and on which record. The log only grows; entries are never changed or deleted.
 */
public class AuditLog {

    private final UUID id;
    private final AuditOperation operation;
    private final LocalDateTime date;
    private final UUID performedByUserId;
    private final UserRole performedByRole;
    private final UUID affectedEntityId;
    private final Map<String, String> details;

    public AuditLog(AuditOperation operation, User performedBy,
                    UUID affectedEntityId, Map<String, String> details) {
        if (operation == null)
            throw new IllegalArgumentException("The audited operation is required.");
        if (performedBy == null)
            throw new IllegalArgumentException("The user who performed the operation is required.");
        if (affectedEntityId == null)
            throw new IllegalArgumentException("The affected record is required.");

        this.id = UUID.randomUUID();
        this.operation = operation;
        this.date = LocalDateTime.now();
        this.performedByUserId = performedBy.getId();
        this.performedByRole = performedBy.getRole();
        this.affectedEntityId = affectedEntityId;
        this.details = copy(details);
    }

    private AuditLog(UUID id, AuditOperation operation, LocalDateTime date, UUID performedByUserId,
                     UserRole performedByRole, UUID affectedEntityId, Map<String, String> details) {
        this.id = id;
        this.operation = operation;
        this.date = date;
        this.performedByUserId = performedByUserId;
        this.performedByRole = performedByRole;
        this.affectedEntityId = affectedEntityId;
        this.details = copy(details);
    }

    /** Rebuilds an audit entry saved by a persistence adapter. It is not for creating new ones. */
    public static AuditLog restore(UUID id, AuditOperation operation, LocalDateTime date,
                                   UUID performedByUserId, UserRole performedByRole,
                                   UUID affectedEntityId, Map<String, String> details) {
        Restore.require(id, "id");
        Restore.require(operation, "operation");
        Restore.require(date, "date");
        Restore.require(performedByUserId, "performedByUserId");
        Restore.require(performedByRole, "performedByRole");
        Restore.require(affectedEntityId, "affectedEntityId");
        return new AuditLog(id, operation, date, performedByUserId, performedByRole, affectedEntityId, details);
    }

    private static Map<String, String> copy(Map<String, String> details) {
        return details == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }

    public UUID getId() { return id; }
    public AuditOperation getOperation() { return operation; }
    public LocalDateTime getDate() { return date; }
    public UUID getPerformedByUserId() { return performedByUserId; }
    public UserRole getPerformedByRole() { return performedByRole; }
    public UUID getAffectedEntityId() { return affectedEntityId; }
    public Map<String, String> getDetails() { return details; }
}
'@

Escribir "src\main\java\application\domain\enums\AuditOperation.java" @'
package application.domain.enums;

/** Business operations that leave a record in the audit log. */
public enum AuditOperation {
    USER_REGISTRATION,
    USER_BLOCKING,
    USER_ACTIVATION,
    SELLER_ONBOARDING,
    WAREHOUSE_REGISTRATION,
    PRODUCT_REGISTRATION,
    PRODUCT_STATUS_CHANGE,
    INVENTORY_REGISTRATION,
    INVENTORY_ADJUSTMENT,
    INVENTORY_STATUS_CHANGE,
    INVENTORY_RESTOCK,
    ORDER_CONFIRMATION,
    ORDER_PAYMENT,
    ORDER_SHIPMENT,
    ORDER_DELIVERY,
    RETURN_REQUEST,
    RETURN_APPROVAL,
    RETURN_REJECTION,
    REFUND_PROCESSING
}
'@

Escribir "src\main\java\application\domain\ports\out\AuditLogRepositoryPort.java" @'
package application.domain.ports.out;

import application.domain.entities.AuditLog;

import java.util.List;
import java.util.UUID;

/** Append-only audit trail. Implemented with MongoDB in the persistence adapters. */
public interface AuditLogRepositoryPort {
    void save(AuditLog auditLog);
    List<AuditLog> findAll();
    List<AuditLog> findByUser(UUID userId);
    List<AuditLog> findByAffectedEntity(UUID entityId);
}
'@

Escribir "src\main\java\application\domain\ports\in\AdminPort.java" @'
package application.domain.ports.in;

import application.domain.entities.AuditLog;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;

import java.util.List;

/** Operations of the ADMIN role: users, sellers, warehouses and after-sales decisions. */
public interface AdminPort {

    User registerUser(User admin, User newUser);

    User blockUser(User admin, User target);

    User activateUser(User admin, User target);

    Seller onboardSeller(User admin, User sellerUser, Warehouse firstWarehouse);

    Warehouse registerMarketplaceWarehouse(User admin, Warehouse warehouse);

    Warehouse registerSellerWarehouse(User admin, Seller seller, Warehouse warehouse);

    ReturnRequest approveReturn(User admin, ReturnRequest returnRequest);

    ReturnRequest rejectReturn(User admin, ReturnRequest returnRequest);

    Refund processRefund(User admin, Refund refund);

    List<AuditLog> consultAuditLog(User admin);
}
'@

Escribir "src\main\java\application\domain\ports\in\SupervisorPort.java" @'
package application.domain.ports.in;

import application.domain.entities.AuditLog;
import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.User;
import application.domain.entities.Warehouse;

import java.util.List;
import java.util.UUID;

/** Read-only follow-up operations of the SUPERVISOR role (OBJ-12). */
public interface SupervisorPort {

    List<User> consultUsers(User supervisor);

    List<Warehouse> consultWarehouses(User supervisor);

    List<Inventory> consultInventory(User supervisor);

    List<Order> consultOrders(User supervisor);

    List<AuditLog> consultAuditLog(User supervisor);

    List<AuditLog> consultAuditLogOf(User supervisor, UUID entityId);
}
'@

Escribir "src\main\java\application\adapters\useCases\AdminUseCaseImpl.java" @'
package application.adapters.useCases;

import application.domain.entities.AuditLog;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.AuditOperation;
import application.domain.enums.BusinessProcess;
import application.domain.ports.in.AdminPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.AuthorizationService;
import application.domain.services.ConsultAuditLogService;
import application.domain.services.GenerateRefundService;
import application.domain.services.OnboardSellerService;
import application.domain.services.ProcessRefundService;
import application.domain.services.RegisterAuditLogService;
import application.domain.services.RegisterUserService;
import application.domain.services.RegisterWarehouseService;
import application.domain.services.ResolveReturnService;

import java.util.List;
import java.util.Map;

/** Operations of the ADMIN role: users, sellers, warehouses and after-sales decisions. */
public class AdminUseCaseImpl implements AdminPort {

    private final AuthorizationService authorizationService;
    private final RegisterUserService registerUserService;
    private final OnboardSellerService onboardSellerService;
    private final RegisterWarehouseService registerWarehouseService;
    private final ResolveReturnService resolveReturnService;
    private final GenerateRefundService generateRefundService;
    private final ProcessRefundService processRefundService;
    private final UserRepositoryPort userRepository;
    private final SellerRepositoryPort sellerRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final ReturnRequestRepositoryPort returnRepository;
    private final RefundRepositoryPort refundRepository;
    private final RegisterAuditLogService auditLogService;
    private final ConsultAuditLogService consultAuditLogService;

    public AdminUseCaseImpl(AuthorizationService authorizationService,
                            RegisterUserService registerUserService,
                            OnboardSellerService onboardSellerService,
                            RegisterWarehouseService registerWarehouseService,
                            ResolveReturnService resolveReturnService,
                            GenerateRefundService generateRefundService,
                            ProcessRefundService processRefundService,
                            UserRepositoryPort userRepository,
                            SellerRepositoryPort sellerRepository,
                            WarehouseRepositoryPort warehouseRepository,
                            ReturnRequestRepositoryPort returnRepository,
                            RefundRepositoryPort refundRepository,
                            RegisterAuditLogService auditLogService,
                            ConsultAuditLogService consultAuditLogService) {
        this.authorizationService = authorizationService;
        this.registerUserService = registerUserService;
        this.onboardSellerService = onboardSellerService;
        this.registerWarehouseService = registerWarehouseService;
        this.resolveReturnService = resolveReturnService;
        this.generateRefundService = generateRefundService;
        this.processRefundService = processRefundService;
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.returnRepository = returnRepository;
        this.refundRepository = refundRepository;
        this.auditLogService = auditLogService;
        this.consultAuditLogService = consultAuditLogService;
    }

    @Override
    public User registerUser(User admin, User newUser) {
        authorizationService.requirePermission(admin, BusinessProcess.USER_MANAGEMENT);
        if (newUser == null)
            throw new IllegalArgumentException("The user to register is required.");
        User registered = registerUserService.register(admin, newUser.getFullName(), newUser.getEmail(),
                newUser.getDocument(), newUser.getRole());
        auditLogService.register(AuditOperation.USER_REGISTRATION, admin, registered.getId(),
                Map.of("role", registered.getRole().name()));
        return registered;
    }

    @Override
    public User blockUser(User admin, User target) {
        User stored = targetOf(admin, target);
        if (stored.getId().equals(admin.getId()))
            throw new IllegalStateException("An admin cannot block their own user.");
        stored.block();
        userRepository.save(stored);
        auditLogService.register(AuditOperation.USER_BLOCKING, admin, stored.getId(), Map.of());
        return stored;
    }

    @Override
    public User activateUser(User admin, User target) {
        User stored = targetOf(admin, target);
        stored.activate();
        userRepository.save(stored);
        auditLogService.register(AuditOperation.USER_ACTIVATION, admin, stored.getId(), Map.of());
        return stored;
    }

    /** Creates the seller profile and its first warehouse in one step (RN-01). */
    @Override
    public Seller onboardSeller(User admin, User sellerUser, Warehouse firstWarehouse) {
        authorizationService.requirePermission(admin, BusinessProcess.SELLER_REGISTRATION);
        User storedUser = Lookup.reload(sellerUser, User::getId, userRepository::findById, "User");
        if (firstWarehouse == null)
            throw new IllegalArgumentException("The first warehouse is required.");
        if (sellerRepository.existsByUserId(storedUser.getId()))
            throw new IllegalStateException("The user already has a seller profile.");
        if (warehouseRepository.existsByName(firstWarehouse.getName()))
            throw new IllegalStateException("A warehouse with that name already exists.");

        OnboardSellerService.Result result = onboardSellerService.onboard(admin, storedUser,
                firstWarehouse.getName(), firstWarehouse.getType(), firstWarehouse.getAddress());

        sellerRepository.save(result.seller());
        warehouseRepository.save(result.warehouse());
        auditLogService.register(AuditOperation.SELLER_ONBOARDING, admin, result.seller().getId(),
                Map.of("user", storedUser.getId().toString(), "warehouse", result.warehouse().getName()));
        return result.seller();
    }

    @Override
    public Warehouse registerMarketplaceWarehouse(User admin, Warehouse warehouse) {
        if (warehouse == null)
            throw new IllegalArgumentException("Warehouse is required.");
        Warehouse registered = registerWarehouseService.registerForMarketplace(admin, warehouse.getName(),
                warehouse.getType(), warehouse.getAddress());
        auditWarehouse(admin, registered);
        return registered;
    }

    @Override
    public Warehouse registerSellerWarehouse(User admin, Seller seller, Warehouse warehouse) {
        authorizationService.requirePermission(admin, BusinessProcess.WAREHOUSE_MANAGEMENT);
        Seller storedSeller = Lookup.reload(seller, Seller::getId, sellerRepository::findById, "Seller");
        if (warehouse == null)
            throw new IllegalArgumentException("Warehouse is required.");
        Warehouse registered = registerWarehouseService.registerForSeller(admin, storedSeller, warehouse.getName(),
                warehouse.getType(), warehouse.getAddress());
        auditWarehouse(admin, registered);
        return registered;
    }

    /** Approving a return also generates its refund, which stays pending until processed. */
    @Override
    public ReturnRequest approveReturn(User admin, ReturnRequest returnRequest) {
        ReturnRequest stored = reloadReturn(returnRequest);
        resolveReturnService.approve(admin, stored);
        Refund refund = generateRefundService.generate(stored,
                refundRepository.findByOrder(stored.getOrder().getId()).map(List::of).orElse(List.of()));
        returnRepository.save(stored);
        refundRepository.save(refund);
        auditLogService.register(AuditOperation.RETURN_APPROVAL, admin, stored.getId(),
                Map.of("refund", refund.getId().toString(), "amount", refund.getAmount().amount().toPlainString()));
        return stored;
    }

    @Override
    public ReturnRequest rejectReturn(User admin, ReturnRequest returnRequest) {
        ReturnRequest stored = reloadReturn(returnRequest);
        resolveReturnService.reject(admin, stored);
        returnRepository.save(stored);
        auditLogService.register(AuditOperation.RETURN_REJECTION, admin, stored.getId(), Map.of());
        return stored;
    }

    @Override
    public Refund processRefund(User admin, Refund refund) {
        Refund stored = Lookup.reload(refund, Refund::getId, refundRepository::findById, "Refund");
        processRefundService.process(admin, stored);
        refundRepository.save(stored);
        auditLogService.register(AuditOperation.REFUND_PROCESSING, admin, stored.getId(),
                Map.of("amount", stored.getAmount().amount().toPlainString()));
        return stored;
    }

    @Override
    public List<AuditLog> consultAuditLog(User admin) {
        return consultAuditLogService.consultAll(admin);
    }

    private void auditWarehouse(User admin, Warehouse warehouse) {
        auditLogService.register(AuditOperation.WAREHOUSE_REGISTRATION, admin, warehouse.getId(),
                Map.of("name", warehouse.getName(), "ownership", warehouse.getOwnership().name()));
    }

    private User targetOf(User admin, User target) {
        authorizationService.requirePermission(admin, BusinessProcess.USER_MANAGEMENT);
        return Lookup.reload(target, User::getId, userRepository::findById, "User");
    }

    private ReturnRequest reloadReturn(ReturnRequest returnRequest) {
        return Lookup.reload(returnRequest, ReturnRequest::getId, returnRepository::findById, "Return request");
    }
}
'@

Escribir "src\main\java\application\adapters\useCases\BuyerUseCaseImpl.java" @'
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
import application.domain.enums.AuditOperation;
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
import application.domain.services.RegisterAuditLogService;
import application.domain.services.RequestReturnService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;

import java.util.List;
import java.util.Map;
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
    private final RegisterAuditLogService auditLogService;

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
                            ReturnRequestRepositoryPort returnRepository,
                            RegisterAuditLogService auditLogService) {
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
        this.auditLogService = auditLogService;
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
        auditLogService.register(AuditOperation.ORDER_CONFIRMATION, user, result.order().getId(),
                Map.of("total", result.order().calculateTotal().amount().toPlainString()));
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
        auditLogService.register(AuditOperation.ORDER_PAYMENT, user, stored.getId(),
                Map.of("invoice", invoice.getId().toString(), "status", stored.getStatus().name()));
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
        auditLogService.register(AuditOperation.RETURN_REQUEST, user, returnRequest.getId(),
                Map.of("order", stored.getId().toString()));
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
'@

Escribir "src\main\java\application\adapters\useCases\InventoryOperationsSupport.java" @'
package application.adapters.useCases;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.AuditOperation;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.AdjustInventoryService;
import application.domain.services.MarkInventoryService;
import application.domain.services.RegisterAuditLogService;
import application.domain.services.RegisterInitialInventoryService;
import application.domain.services.RestockInventoryService;

import java.util.List;
import java.util.Map;

/**
 * Inventory operations shared by the seller and the logistic operator. Both roles run
 * the same domain services; the only difference is the seller profile: the seller passes
 * their own so ownership is checked, the operator passes null because they may operate
 * on any product.
 */
public class InventoryOperationsSupport {

    private final RegisterInitialInventoryService registerInitialInventoryService;
    private final AdjustInventoryService adjustInventoryService;
    private final MarkInventoryService markInventoryService;
    private final RestockInventoryService restockInventoryService;
    private final ProductRepositoryPort productRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final ReturnRequestRepositoryPort returnRepository;
    private final RegisterAuditLogService auditLogService;

    public InventoryOperationsSupport(RegisterInitialInventoryService registerInitialInventoryService,
                                      AdjustInventoryService adjustInventoryService,
                                      MarkInventoryService markInventoryService,
                                      RestockInventoryService restockInventoryService,
                                      ProductRepositoryPort productRepository,
                                      WarehouseRepositoryPort warehouseRepository,
                                      InventoryRepositoryPort inventoryRepository,
                                      InventoryMovementRepositoryPort movementRepository,
                                      ReturnRequestRepositoryPort returnRepository,
                                      RegisterAuditLogService auditLogService) {
        this.registerInitialInventoryService = registerInitialInventoryService;
        this.adjustInventoryService = adjustInventoryService;
        this.markInventoryService = markInventoryService;
        this.restockInventoryService = restockInventoryService;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.returnRepository = returnRepository;
        this.auditLogService = auditLogService;
    }

    public Inventory registerInitialInventory(User user, Seller seller, Product product,
                                              Warehouse warehouse, int quantity) {
        Product storedProduct = Lookup.reload(product, Product::getId, productRepository::findById, "Product");
        Warehouse storedWarehouse = Lookup.reload(warehouse, Warehouse::getId, warehouseRepository::findById, "Warehouse");
        RegisterInitialInventoryService.Result result = registerInitialInventoryService.register(
                user, seller, storedProduct, storedWarehouse, quantity,
                inventoryRepository.findByProduct(storedProduct.getId()));
        inventoryRepository.save(result.inventory());
        movementRepository.saveAll(result.movements());
        auditLogService.register(AuditOperation.INVENTORY_REGISTRATION, user, result.inventory().getId(),
                Map.of("quantity", String.valueOf(quantity)));
        return result.inventory();
    }

    public Inventory adjust(User user, Seller seller, Inventory inventory, int difference, String reason) {
        Inventory stored = reload(inventory);
        InventoryMovement movement = adjustInventoryService.adjust(user, seller, stored, difference, reason);
        inventoryRepository.save(stored);
        movementRepository.saveAll(List.of(movement));
        auditLogService.register(AuditOperation.INVENTORY_ADJUSTMENT, user, stored.getId(),
                Map.of("difference", String.valueOf(difference), "reason", movement.getReason()));
        return stored;
    }

    public Inventory markDamaged(User user, Seller seller, Inventory inventory) {
        Inventory stored = reload(inventory);
        markInventoryService.markDamaged(user, seller, stored);
        inventoryRepository.save(stored);
        auditStatusChange(user, stored);
        return stored;
    }

    public Inventory block(User user, Seller seller, Inventory inventory) {
        Inventory stored = reload(inventory);
        markInventoryService.block(user, seller, stored);
        inventoryRepository.save(stored);
        auditStatusChange(user, stored);
        return stored;
    }

    public Inventory enable(User user, Seller seller, Inventory inventory) {
        Inventory stored = reload(inventory);
        markInventoryService.enable(user, seller, stored);
        inventoryRepository.save(stored);
        auditStatusChange(user, stored);
        return stored;
    }

    public Inventory restockFromReturn(User user, Seller seller, Inventory inventory,
                                       ReturnRequest returnRequest, int units) {
        Inventory stored = reload(inventory);
        ReturnRequest storedReturn = Lookup.reload(returnRequest, ReturnRequest::getId,
                returnRepository::findById, "Return request");
        InventoryMovement movement = restockInventoryService.restock(user, seller, stored, units, storedReturn);
        inventoryRepository.save(stored);
        returnRepository.save(storedReturn);
        movementRepository.saveAll(List.of(movement));
        auditLogService.register(AuditOperation.INVENTORY_RESTOCK, user, stored.getId(),
                Map.of("units", String.valueOf(units), "return", storedReturn.getId().toString()));
        return stored;
    }

    private void auditStatusChange(User user, Inventory inventory) {
        auditLogService.register(AuditOperation.INVENTORY_STATUS_CHANGE, user, inventory.getId(),
                Map.of("status", inventory.getStatus().name()));
    }

    private Inventory reload(Inventory inventory) {
        return Lookup.reload(inventory, Inventory::getId, inventoryRepository::findById, "Inventory");
    }
}
'@

Escribir "src\main\java\application\adapters\useCases\LogisticOperatorUseCaseImpl.java" @'
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
import application.domain.enums.AuditOperation;
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
import application.domain.services.RegisterAuditLogService;
import application.domain.services.ShipOrderService;

import java.util.List;
import java.util.Map;

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
    private final RegisterAuditLogService auditLogService;

    public LogisticOperatorUseCaseImpl(AuthorizationService authorizationService,
                                       ShipOrderService shipOrderService,
                                       ConfirmDeliveryService confirmDeliveryService,
                                       InventoryOperationsSupport inventoryOperations,
                                       OrderRepositoryPort orderRepository,
                                       ShipmentRepositoryPort shipmentRepository,
                                       InventoryReservationRepositoryPort reservationRepository,
                                       InventoryMovementRepositoryPort movementRepository,
                                       InventoryRepositoryPort inventoryRepository,
                                       RegisterAuditLogService auditLogService) {
        this.authorizationService = authorizationService;
        this.shipOrderService = shipOrderService;
        this.confirmDeliveryService = confirmDeliveryService;
        this.inventoryOperations = inventoryOperations;
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.reservationRepository = reservationRepository;
        this.movementRepository = movementRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditLogService = auditLogService;
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
        auditLogService.register(AuditOperation.ORDER_SHIPMENT, user, stored.getId(),
                Map.of("shipment", shipment.getId().toString()));
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
        auditLogService.register(AuditOperation.ORDER_DELIVERY, user, stored.getId(),
                Map.of("shipment", shipment.getId().toString()));
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
'@

Escribir "src\main\java\application\adapters\useCases\PublicAccessUseCaseImpl.java" @'
package application.adapters.useCases;

import application.domain.entities.Buyer;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.CheckAvailabilityService;
import application.domain.services.RegisterAuditLogService;
import application.domain.services.RegisterBuyerService;
import application.domain.services.RegisterUserService;
import application.domain.valueobjects.Address;

import java.util.List;
import java.util.Map;

/** Operations anyone can use without signing in: buyer sign-up and the public catalog. */
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final RegisterUserService registerUserService;
    private final RegisterBuyerService registerBuyerService;
    private final CheckAvailabilityService checkAvailabilityService;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final RegisterAuditLogService auditLogService;

    public PublicAccessUseCaseImpl(RegisterUserService registerUserService,
                                   RegisterBuyerService registerBuyerService,
                                   CheckAvailabilityService checkAvailabilityService,
                                   ProductRepositoryPort productRepository,
                                   InventoryRepositoryPort inventoryRepository,
                                   RegisterAuditLogService auditLogService) {
        this.registerUserService = registerUserService;
        this.registerBuyerService = registerBuyerService;
        this.checkAvailabilityService = checkAvailabilityService;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public Buyer registerBuyer(User user, Address mainAddress) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (mainAddress == null)
            throw new IllegalArgumentException("Main address is required.");
        // Without an actor, RegisterUserService only accepts the BUYER role.
        User saved = registerUserService.register(null, user.getFullName(), user.getEmail(),
                user.getDocument(), user.getRole());
        Buyer buyer = registerBuyerService.register(saved, mainAddress);
        auditLogService.register(AuditOperation.USER_REGISTRATION, saved, saved.getId(),
                Map.of("role", saved.getRole().name(), "channel", "public sign-up"));
        return buyer;
    }

    @Override
    public List<Product> consultCatalog() {
        return productRepository.findPublished();
    }

    @Override
    public int consultAvailability(Product product) {
        Product stored = Lookup.reload(product, Product::getId, productRepository::findById, "Product");
        return checkAvailabilityService.availableOf(stored,
                inventoryRepository.findByProduct(stored.getId()));
    }
}
'@

Escribir "src\main\java\application\adapters\useCases\SellerUseCaseImpl.java" @'
package application.adapters.useCases;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.AuditOperation;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.in.SellerPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.AuthorizationService;
import application.domain.services.ChangeProductStatusService;
import application.domain.services.PublishProductService;
import application.domain.services.RegisterAuditLogService;
import application.domain.services.RegisterProductService;

import java.util.List;
import java.util.Map;

/** Operations of the SELLER role: their catalog and the inventory of their products. */
public class SellerUseCaseImpl implements SellerPort {

    private final AuthorizationService authorizationService;
    private final RegisterProductService registerProductService;
    private final PublishProductService publishProductService;
    private final ChangeProductStatusService changeProductStatusService;
    private final InventoryOperationsSupport inventoryOperations;
    private final SellerRepositoryPort sellerRepository;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final RegisterAuditLogService auditLogService;

    public SellerUseCaseImpl(AuthorizationService authorizationService,
                             RegisterProductService registerProductService,
                             PublishProductService publishProductService,
                             ChangeProductStatusService changeProductStatusService,
                             InventoryOperationsSupport inventoryOperations,
                             SellerRepositoryPort sellerRepository,
                             ProductRepositoryPort productRepository,
                             InventoryRepositoryPort inventoryRepository,
                             RegisterAuditLogService auditLogService) {
        this.authorizationService = authorizationService;
        this.registerProductService = registerProductService;
        this.publishProductService = publishProductService;
        this.changeProductStatusService = changeProductStatusService;
        this.inventoryOperations = inventoryOperations;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditLogService = auditLogService;
    }

    /** The product that arrives is a draft: the stored one always belongs to the signed-in seller. */
    @Override
    public Product registerProduct(User user, Product product) {
        Seller seller = sellerOf(user);
        if (product == null)
            throw new IllegalArgumentException("Product is required.");
        Product registered = registerProductService.register(user, seller, product.getName(),
                product.getDescription(), product.getType(), product.getPrice());
        if (!product.getVariants().isEmpty()) {
            product.getVariants().forEach(registered::addVariant);
            productRepository.save(registered);
        }
        auditLogService.register(AuditOperation.PRODUCT_REGISTRATION, user, registered.getId(),
                Map.of("name", registered.getName(), "type", registered.getType().name()));
        return registered;
    }

    @Override
    public Product publishProduct(User user, Product product) {
        Seller seller = sellerOf(user);
        Product stored = reload(product);
        publishProductService.publish(user, seller, stored, inventoryRepository.findByProduct(stored.getId()));
        productRepository.save(stored);
        auditStatusChange(user, stored);
        return stored;
    }

    @Override
    public Product suspendProduct(User user, Product product) {
        Seller seller = sellerOf(user);
        Product stored = reload(product);
        changeProductStatusService.suspend(user, seller, stored);
        productRepository.save(stored);
        auditStatusChange(user, stored);
        return stored;
    }

    @Override
    public Product discontinueProduct(User user, Product product) {
        Seller seller = sellerOf(user);
        Product stored = reload(product);
        changeProductStatusService.discontinue(user, seller, stored,
                inventoryRepository.findByProduct(stored.getId()));
        productRepository.save(stored);
        auditStatusChange(user, stored);
        return stored;
    }

    @Override
    public List<Product> consultMyProducts(User user) {
        authorizationService.requirePermission(user, BusinessProcess.PRODUCT_REGISTRATION);
        return productRepository.findBySeller(sellerOf(user).getId());
    }

    @Override
    public Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity) {
        return inventoryOperations.registerInitialInventory(user, sellerOf(user), product, warehouse, quantity);
    }

    @Override
    public Inventory adjustInventory(User user, Inventory inventory, int difference, String reason) {
        return inventoryOperations.adjust(user, sellerOf(user), inventory, difference, reason);
    }

    @Override
    public Inventory markInventoryDamaged(User user, Inventory inventory) {
        return inventoryOperations.markDamaged(user, sellerOf(user), inventory);
    }

    @Override
    public Inventory blockInventory(User user, Inventory inventory) {
        return inventoryOperations.block(user, sellerOf(user), inventory);
    }

    @Override
    public Inventory enableInventory(User user, Inventory inventory) {
        return inventoryOperations.enable(user, sellerOf(user), inventory);
    }

    @Override
    public Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units) {
        return inventoryOperations.restockFromReturn(user, sellerOf(user), inventory, returnRequest, units);
    }

    private void auditStatusChange(User user, Product product) {
        auditLogService.register(AuditOperation.PRODUCT_STATUS_CHANGE, user, product.getId(),
                Map.of("status", product.getStatus().name()));
    }

    private Seller sellerOf(User user) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (!user.hasRole(UserRole.SELLER))
            throw new AccessDeniedException("The user does not have the seller role.");
        return sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Seller profile"));
    }

    private Product reload(Product product) {
        return Lookup.reload(product, Product::getId, productRepository::findById, "Product");
    }
}
'@

Escribir "src\main\java\application\adapters\useCases\SupervisorUseCaseImpl.java" @'
package application.adapters.useCases;

import application.domain.entities.AuditLog;
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
import application.domain.services.ConsultAuditLogService;

import java.util.List;
import java.util.UUID;

/** Read-only follow-up of the SUPERVISOR role (OBJ-12). Nothing here changes data. */
public class SupervisorUseCaseImpl implements SupervisorPort {

    private final AuthorizationService authorizationService;
    private final UserRepositoryPort userRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final OrderRepositoryPort orderRepository;
    private final ConsultAuditLogService consultAuditLogService;

    public SupervisorUseCaseImpl(AuthorizationService authorizationService,
                                 UserRepositoryPort userRepository,
                                 WarehouseRepositoryPort warehouseRepository,
                                 InventoryRepositoryPort inventoryRepository,
                                 OrderRepositoryPort orderRepository,
                                 ConsultAuditLogService consultAuditLogService) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.consultAuditLogService = consultAuditLogService;
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

    @Override
    public List<AuditLog> consultAuditLog(User supervisor) {
        return consultAuditLogService.consultAll(supervisor);
    }

    @Override
    public List<AuditLog> consultAuditLogOf(User supervisor, UUID entityId) {
        return consultAuditLogService.consultByEntity(supervisor, entityId);
    }

    private void requireReports(User user) {
        authorizationService.requirePermission(user, BusinessProcess.ADMINISTRATIVE_REPORTS);
    }
}
'@

Escribir "..\SDD\README.md" @'
# SDD de NexusMarket

Especificación del diseño de software de NexusMarket, construida a partir de la especificación funcional del negocio. La documentación está en español; el código Java está en inglés, y cada clase, atributo y método nombrado aquí coincide con el código en `nexusmarket/src/main/java/application/`.

## Contenido

```text
SDD
├── domain
│   ├── Domain Model.md            entidades: descripción, atributos, comportamiento, relaciones y reglas
│   ├── Domain Value Objects.md    objetos de valor y enumeraciones con sus valores permitidos
│   ├── Domain Services.md         resumen de los servicios de dominio por subdominio
│   ├── services/                  detalle de cada servicio, un archivo por subdominio
│   ├── Input-ports.md             puertos de entrada por rol
│   └── Output-ports.md            puertos de salida (repositorios)
└── Adapters
    └── Use-cases-adapters.md      casos de uso que implementan los puertos por rol
```

## Estructura del código

```text
application
├── domain
│   ├── entities        17 entidades
│   ├── valueobjects    5 objetos de valor
│   ├── enums           16 enumeraciones
│   ├── exceptions      AccessDeniedException, EntityNotFoundException
│   ├── services        34 servicios de dominio
│   └── ports
│       ├── in          6 puertos de entrada, uno por rol más el acceso público
│       └── out         15 puertos de salida (14 MySQL, 1 MongoDB)
└── adapters
    └── useCases        6 casos de uso, InventoryOperationsSupport y Lookup
```

Las reglas del dominio y los flujos de los casos de uso están cubiertos por 230 pruebas automáticas con JUnit en `nexusmarket/src/test/java/application/`.
'@

Escribir "..\SDD\Adapters\Use-cases-adapters.md" @'
# Use Cases Adapters

## 1. Descripción general

Los adaptadores de casos de uso están en `adapters/useCases/`. Son las implementaciones concretas de los puertos de entrada por rol de `domain/ports/in/` y conectan las llamadas que llegan desde afuera (los controladores REST de la Fase C) con los servicios de dominio.

No contienen reglas de negocio. Cada operación sigue siempre los mismos pasos: **busca** en los repositorios los datos que necesita, **llama** al servicio de dominio que tiene la regla y **guarda** el resultado.

---

## 2. Rol en la arquitectura

```text
REST Controller (adapters/rest, Fase C)
      |
      v  llama al puerto
Role Input Port (domain/ports/in)
      ^
      |  implementado por
Use Case Adapter (adapters/useCases)
      |
      +--> Domain Service (domain/services): reglas de negocio
      |
      +--> Output Port (domain/ports/out): leer y guardar
```

---

## 3. Clases y dependencias

| Puerto | Clase | Servicios inyectados | Repositorios inyectados |
|---|---|---|---|
| `PublicAccessPort` | `PublicAccessUseCaseImpl` | `RegisterUserService`, `RegisterBuyerService`, `CheckAvailabilityService`, `RegisterAuditLogService` | `ProductRepositoryPort`, `InventoryRepositoryPort` |
| `BuyerPort` | `BuyerUseCaseImpl` | `AuthorizationService`, `AddToCartService`, `ConfirmOrderService`, `PayOrderService`, `IssueInvoiceService`, `DeliverDigitalOrderService`, `CreateShipmentService`, `RequestReturnService`, `RegisterAuditLogService` | `BuyerRepositoryPort`, `CartRepositoryPort`, `ProductRepositoryPort`, `InventoryRepositoryPort`, `InventoryReservationRepositoryPort`, `InventoryMovementRepositoryPort`, `OrderRepositoryPort`, `InvoiceRepositoryPort`, `ShipmentRepositoryPort`, `ReturnRequestRepositoryPort` |
| `SellerPort` | `SellerUseCaseImpl` | `AuthorizationService`, `RegisterProductService`, `PublishProductService`, `ChangeProductStatusService`, `InventoryOperationsSupport`, `RegisterAuditLogService` | `SellerRepositoryPort`, `ProductRepositoryPort`, `InventoryRepositoryPort` |
| `LogisticOperatorPort` | `LogisticOperatorUseCaseImpl` | `AuthorizationService`, `ShipOrderService`, `ConfirmDeliveryService`, `InventoryOperationsSupport`, `RegisterAuditLogService` | `OrderRepositoryPort`, `ShipmentRepositoryPort`, `InventoryReservationRepositoryPort`, `InventoryMovementRepositoryPort`, `InventoryRepositoryPort` |
| `AdminPort` | `AdminUseCaseImpl` | `AuthorizationService`, `RegisterUserService`, `OnboardSellerService`, `RegisterWarehouseService`, `ResolveReturnService`, `GenerateRefundService`, `ProcessRefundService`, `RegisterAuditLogService`, `ConsultAuditLogService` | `UserRepositoryPort`, `SellerRepositoryPort`, `WarehouseRepositoryPort`, `ReturnRequestRepositoryPort`, `RefundRepositoryPort` |
| `SupervisorPort` | `SupervisorUseCaseImpl` | `AuthorizationService`, `ConsultAuditLogService` | `UserRepositoryPort`, `WarehouseRepositoryPort`, `InventoryRepositoryPort`, `OrderRepositoryPort` |

Clases de apoyo:

* `InventoryOperationsSupport`: las seis operaciones de inventario que comparten el vendedor y el operador logístico. El vendedor envía su perfil de `Seller` para que se valide que el producto es suyo; el operador envía `null` porque puede operar sobre cualquier producto. Inyecta `RegisterInitialInventoryService`, `AdjustInventoryService`, `MarkInventoryService`, `RestockInventoryService`, `ProductRepositoryPort`, `WarehouseRepositoryPort`, `InventoryRepositoryPort`, `InventoryMovementRepositoryPort`, `ReturnRequestRepositoryPort`, `RegisterAuditLogService`.
* `InventoryOperationsSupport` también deja el registro de auditoría de cada operación de inventario.
* `Lookup`: recarga desde su repositorio cada objeto que llega a un caso de uso. La capa REST solo conoce identificadores, así que esto garantiza que el objeto existe y que se trabaja con el estado guardado. Si no existe, lanza `EntityNotFoundException`.

---

## 4. Flujos que orquestan

Algunas operaciones encadenan varios servicios de dominio. Estas son las decisiones de negocio que quedaron en esa orquestación:

* **`BuyerUseCaseImpl.confirmOrder`.** Reúne el inventario de los productos del carrito y llama a `ConfirmOrderService` con la dirección elegida (o `null` para usar la principal). Guarda el pedido, las reservas, los movimientos, el inventario y el carrito vacío.
* **`BuyerUseCaseImpl.payOrder`.** Registra el pago con `PayOrderService` y emite la factura con `IssueInvoiceService`. Si el pedido es solo digital, lo entrega de inmediato con `DeliverDigitalOrderService`; si no, crea el envío con `CreateShipmentService` hacia la dirección guardada en el pedido.
* **`LogisticOperatorUseCaseImpl.consultOrdersToShip`.** Devuelve los pedidos `PAID` que tienen productos físicos.
* **`LogisticOperatorUseCaseImpl.shipOrder`.** Busca el envío y las reservas del pedido, despacha con `ShipOrderService` y guarda el inventario, las reservas confirmadas y los movimientos `SALE`.
* **`AdminUseCaseImpl.onboardSeller`.** Verifica que el usuario no tenga ya perfil de vendedor y que el nombre de la bodega esté libre; luego crea vendedor y primera bodega con `OnboardSellerService` (RN-01).
* **`AdminUseCaseImpl.approveReturn`.** Aprueba la devolución con `ResolveReturnService` y genera el reembolso con `GenerateRefundService`, que queda `PENDING` hasta que se procese.
* **`AdminUseCaseImpl.blockUser`.** Exige `USER_MANAGEMENT` y no permite que un administrador se bloquee a sí mismo.
* **`PublicAccessUseCaseImpl.registerBuyer`.** Crea el usuario con `RegisterUserService` (sin actor solo acepta el rol `BUYER`) y su perfil con `RegisterBuyerService`.
* **`SupervisorUseCaseImpl`.** Todas sus consultas exigen `ADMINISTRATIVE_REPORTS`; ninguna modifica datos. También consulta la auditoría completa o la de un registro (`consultAuditLogOf`).
* **Auditoría.** Cada operación que cambia datos termina llamando a `RegisterAuditLogService` con su `AuditOperation`, el usuario y el registro afectado. Se llama al final, así que una operación que falla no deja registro.

---

## 5. Ejemplo del patrón

```java
@Override
public Order shipOrder(User user, Order order) {
    authorizationService.requirePermission(user, BusinessProcess.SHIPPING_OPERATIONS);
    Order stored = Lookup.reload(order, Order::getId, orderRepository::findById, "Order");      // buscar
    Shipment shipment = shipmentOf(stored);
    List<InventoryReservation> reservations = reservationRepository.findByOrder(stored.getId());

    List<InventoryMovement> movements = shipOrderService.ship(user, stored, shipment, reservations); // regla

    inventoryRepository.saveAll(...);                                                            // guardar
    reservationRepository.saveAll(reservations);
    movementRepository.saveAll(movements);
    shipmentRepository.save(shipment);
    orderRepository.save(stored);
    return stored;
}
```

---

## 6. Pendiente para la Fase B

Las clases todavía no tienen `@Service`, porque Spring necesita las implementaciones reales de los repositorios para construirlas. En la Fase B se agregan los adaptadores de MySQL, las anotaciones y una clase de configuración que registra los servicios de dominio como beans, sin poner anotaciones de Spring dentro de `domain`.

Mientras tanto, las pruebas de `src/test/java/application/adapters/useCases/` arman los seis casos de uso con los servicios reales y repositorios en memoria, y recorren el flujo completo del negocio.
'@

Escribir "..\SDD\domain\Domain Model.md" @'
# Domain Model

## Introducción

El modelo de dominio representa las entidades del negocio de NexusMarket, un marketplace que intermedia entre compradores y vendedores y administra el catálogo, el inventario distribuido por bodegas, los pedidos, la facturación, la logística y la posventa. Toma como base la especificación funcional del sistema.

Las entidades tienen identidad propia, cambian de estado con el tiempo y protegen sus propias reglas: un inventario nunca queda negativo, un pedido entregado ya no se modifica y un producto descontinuado no vuelve a publicarse. Las reglas que involucran a varias entidades a la vez se documentan en `Domain Services.md`.

El código está en inglés y esta documentación en español. Cada nombre de clase, atributo y método de este documento coincide exactamente con el código en `nexusmarket/src/main/java/application/domain/entities/`.

---

# Organización por área

```text
Usuarios: User, Buyer, Seller
Bodegas: Warehouse
Catálogo: Product
Inventario: Inventory, InventoryReservation, InventoryMovement
Compra: Cart, CartItem
Pedidos: Order, OrderItem
Facturación: Invoice
Logística: Shipment
Posventa: ReturnRequest, Refund
Auditoría: AuditLog
```

---

# Relaciones del dominio

```text
User ──(userId)──> Buyer
User ──(userId)──> Seller
Seller ──(sellerId)──> Product
Seller ──(sellerId)──> Warehouse (ownership = SELLER)
Product + Warehouse ──> Inventory
Inventory ──> InventoryReservation (por orderId)
Inventory ──> InventoryMovement
Buyer ──> Cart ──> CartItem ──> Product
Cart ──(al confirmarse)──> Order ──> OrderItem
Order ──> Invoice
Order ──> Shipment
Order ──> ReturnRequest
Order ──> Refund
User ──(performedByUserId)──> AuditLog ──(affectedEntityId)──> cualquier registro
```

---

# Entidades

---

# User

**Área:** Usuarios

## Descripción

Representa a una persona registrada en la plataforma. Es la base de la identificación y del control de acceso: cada operación del dominio recibe el `User` que la ejecuta.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del usuario. |
| `fullName` | `FullName` | Nombres y apellidos de la persona. |
| `email` | `Email` | Correo de acceso y comunicación. Único en la plataforma. |
| `document` | `IdentityDocument` | Documento de identidad. Único en la plataforma. |
| `role` | `UserRole` | Rol único del usuario dentro del sistema (RG-02). No cambia después de creado. |
| `status` | `UserStatus` | Estado operativo del usuario. Nace `ACTIVE`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static User restore(UUID id, FullName fullName, Email email, IdentityDocument document, UserRole role, UserStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void block()` | Bloquea al usuario. Falla si ya estaba bloqueado. |
| `void activate()` | Reactiva al usuario. Falla si ya estaba activo. |
| `boolean isActive()` | Indica si el usuario está activo. |
| `boolean hasRole(UserRole expected)` | Indica si el usuario tiene el rol indicado. |

## Relaciones

* Un `User` con rol `BUYER` se asocia a un `Buyer`, y uno con rol `SELLER` a un `Seller`, por medio de `userId`.

## Reglas de negocio

* Correo, documento, nombre y rol son obligatorios.
* El correo y el documento deben ser únicos en la plataforma (validación crítica de Usuarios); la unicidad la comprueba `RegisterUserService` por medio de `UserRepositoryPort`.
* Cada usuario tiene un único rol (RG-02).

---

# Buyer

**Área:** Usuarios

## Descripción

Perfil comercial de un usuario con rol de comprador. Guarda la información que necesita para comprar: sus direcciones de entrega y su estado comercial.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del comprador. |
| `userId` | `UUID` | Identificador del `User` al que pertenece este perfil. |
| `mainAddress` | `Address` | Dirección principal de entrega. Obligatoria. |
| `additionalAddresses` | `List<Address>` | Direcciones secundarias de entrega. Vacía por defecto. |
| `commercialStatus` | `CommercialStatus` | Condición del comprador para realizar compras. Nace `ENABLED`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Buyer restore(UUID id, UUID userId, Address mainAddress, List<Address> additionalAddresses, CommercialStatus commercialStatus)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void addAdditionalAddress(Address address)` | Agrega una dirección secundaria. Rechaza direcciones repetidas. |
| `void removeAdditionalAddress(Address address)` | Quita una dirección secundaria. |
| `void changeMainAddress(Address newAddress)` | Cambia la dirección principal; la anterior pasa a ser adicional. |
| `boolean hasAddress(Address address)` | Indica si una dirección es la principal o una de las adicionales. |
| `void restrict()` | Restringe al comprador para que no pueda comprar. |
| `void enable()` | Vuelve a habilitar al comprador. |
| `boolean canPurchase()` | Indica si el estado comercial permite comprar. |

## Relaciones

* Pertenece a un `User` por medio de `userId`.
* Es dueño de sus `Cart` y de sus `Order`.

## Reglas de negocio

* Un usuario tiene como máximo un perfil de comprador.
* El comprador nunca administra información de otros compradores ni inventarios (Dominio 2).

---

# Seller

**Área:** Usuarios

## Descripción

Perfil de un usuario con rol de vendedor. Identifica al dueño de los productos y de las bodegas de vendedor.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del vendedor. |
| `userId` | `UUID` | Identificador del `User` al que pertenece este perfil. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Seller restore(UUID id, UUID userId)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Pertenece a un `User` por medio de `userId`.
* Es dueño de `Product` y puede tener `Warehouse` propias (por `sellerId`).

## Reglas de negocio

* Los vendedores no se registran por sí mismos: los incorpora un administrador (Dominio 3).
* Un usuario tiene como máximo un perfil de vendedor.

---

# Warehouse

**Área:** Bodegas

## Descripción

Espacio de almacenamiento donde se guarda el inventario. Puede pertenecer al Marketplace o a un vendedor. Como extensión del modelo se contempla la bodega digital, sin dirección física.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único de la bodega. |
| `name` | `String` | Nombre de la bodega. Único en la plataforma. |
| `type` | `WarehouseType` | Tipo de bodega: física o digital. |
| `address` | `Address` | Dirección física. Obligatoria en bodegas físicas y vacía en las digitales. |
| `ownership` | `WarehouseOwnership` | Indica si la bodega es del Marketplace o de un vendedor (Dominio 4). |
| `sellerId` | `UUID` | Vendedor dueño de la bodega. Solo aplica cuando `ownership` es `SELLER`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Warehouse ofMarketplace(String name, WarehouseType type, Address address)` | Crea una bodega del Marketplace. |
| `static Warehouse ofSeller(UUID sellerId, String name, WarehouseType type, Address address)` | Crea una bodega de un vendedor; exige el `sellerId`. |
| `static Warehouse restore(UUID id, String name, WarehouseType type, Address address, WarehouseOwnership ownership, UUID sellerId)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `boolean isDigital()` | Indica si la bodega es digital. |
| `boolean isMarketplaceOwned()` | Indica si la bodega es del Marketplace. |
| `boolean belongsTo(UUID sellerIdToCheck)` | Indica si la bodega pertenece al vendedor indicado. |

## Relaciones

* Contiene registros de `Inventory`.
* Puede pertenecer a un `Seller`.

## Reglas de negocio

* Una bodega física debe tener dirección y una digital no la tiene.
* Una bodega de vendedor siempre tiene propietario; una del Marketplace no.

---

# Product

**Área:** Catálogo

## Descripción

Artículo físico o digital que un vendedor ofrece en el catálogo.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del producto. |
| `sellerId` | `UUID` | Vendedor dueño del producto. |
| `name` | `String` | Nombre del producto. No se repite dentro del mismo vendedor. |
| `description` | `String` | Descripción del producto. |
| `type` | `ProductType` | Físico o digital (Dominio 5). |
| `variants` | `List<String>` | Diferencias de color, talla, modelo, etc. Sin repetidos. |
| `price` | `Money` | Precio unitario. Siempre mayor que cero. |
| `status` | `ProductStatus` | Estado de publicación. Nace `SUSPENDED`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Product restore(UUID id, UUID sellerId, String name, String description, ProductType type, Money price, ProductStatus status, List<String> variants)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void addVariant(String variant)` | Agrega una variante. Rechaza vacías y repetidas. |
| `void changePrice(Money newPrice)` | Cambia el precio, que debe ser mayor que cero. |
| `void publish()` | Publica el producto. Un producto descontinuado no vuelve a publicarse. |
| `void suspend()` | Suspende un producto publicado. |
| `void discontinue()` | Descontinúa el producto de forma definitiva. |
| `boolean isPublished()` | Indica si el producto está publicado. |
| `boolean isPhysical()` | Indica si el producto es físico. |
| `boolean belongsTo(UUID sellerIdToCheck)` | Indica si el producto pertenece al vendedor indicado. |

## Relaciones

* Pertenece a un `Seller`.
* Sus existencias se registran en `Inventory`, una por bodega.

## Reglas de negocio

* Los productos físicos requieren inventario y despacho; los digitales se entregan de inmediato tras el pago.
* Un producto físico solo se publica si tiene existencias disponibles (lo valida `PublishProductService`).

---

# Inventory

**Área:** Inventario

## Descripción

Existencias de un producto en una bodega específica. El inventario es distribuido: un mismo producto puede tener existencias en varias bodegas.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del registro de inventario. |
| `product` | `Product` | Producto al que corresponden las existencias. |
| `warehouse` | `Warehouse` | Bodega donde están las existencias. |
| `quantity` | `int` | Unidades totales en la bodega. |
| `reserved` | `int` | Unidades apartadas para pedidos en curso. |
| `status` | `InventoryStatus` | Estado del inventario. Nace `AVAILABLE`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Inventory restore(UUID id, Product product, Warehouse warehouse, int quantity, int reserved, InventoryStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `int getAvailable()` | Devuelve las unidades disponibles: `quantity - reserved`. |
| `boolean isFor(UUID productId, UUID warehouseId)` | Indica si el registro corresponde al producto y la bodega indicados. |
| `void receive(int units)` | Movimiento de ingreso: suma unidades. |
| `void reserve(int units)` | Movimiento de reserva: aparta unidades disponibles. |
| `void releaseReservation(int units)` | Devuelve unidades reservadas al disponible. |
| `void confirmOutflow(int units)` | Salida por venta: descuenta unidades reservadas. |
| `void adjust(int difference)` | Ajuste positivo o negativo de las existencias. |
| `void restock(int units)` | Movimiento de devolución: reintegra unidades. |
| `void markDamaged()` | Marca el inventario como dañado. Exige que no tenga reservas. |
| `void block()` | Bloquea un inventario disponible. |
| `void enable()` | Vuelve a habilitar un inventario bloqueado. |

## Relaciones

* Pertenece a un `Product` y a una `Warehouse`.
* Sus reservas se registran en `InventoryReservation` y sus movimientos en `InventoryMovement`.

## Reglas de negocio

* No se permiten existencias negativas bajo ninguna circunstancia (Dominio 6).
* No se puede reservar inventario inexistente o marcado como dañado (validación crítica de Inventario).
* Un ajuste no puede dejar menos unidades que las reservadas.

---

# InventoryReservation

**Área:** Inventario

## Descripción

Registra las unidades de un inventario apartadas para un pedido. Permite saber qué deshacer si el pedido no se concreta o qué descontar cuando se despacha.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único de la reserva. |
| `orderId` | `UUID` | Pedido para el que se apartaron las unidades. |
| `inventory` | `Inventory` | Inventario del que se apartaron las unidades. |
| `quantity` | `int` | Unidades reservadas. |
| `date` | `LocalDateTime` | Fecha y hora de la reserva. |
| `status` | `ReservationStatus` | Estado de la reserva. Nace `ACTIVE`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static InventoryReservation create(UUID orderId, Inventory inventory, int quantity)` | Crea la reserva y aparta las unidades en el inventario en el mismo acto. |
| `static InventoryReservation restore(UUID id, UUID orderId, Inventory inventory, int quantity, LocalDateTime date, ReservationStatus status)` | Reconstruye una reserva guardada sin volver a apartar unidades: el inventario guardado ya las cuenta. |
| `void release()` | Libera la reserva y devuelve las unidades al disponible. |
| `void confirmOutflow()` | Confirma la salida por venta de las unidades reservadas. |
| `boolean isActive()` | Indica si la reserva sigue activa. |
| `boolean belongsToOrder(UUID orderIdToCheck)` | Indica si la reserva pertenece al pedido indicado. |

## Relaciones

* Pertenece a un `Inventory` y a un `Order` (por `orderId`).

## Reglas de negocio

* Una reserva se cierra una sola vez: se libera o se confirma, nunca ambas.

---

# InventoryMovement

**Área:** Inventario

## Descripción

Registro histórico de cada movimiento de inventario. Da la trazabilidad que pide la especificación.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del movimiento. |
| `inventoryId` | `UUID` | Inventario afectado. |
| `productId` | `UUID` | Producto afectado. |
| `warehouseId` | `UUID` | Bodega afectada. |
| `type` | `MovementType` | Tipo de movimiento: ingreso, reserva, salida por venta, ajuste o devolución. |
| `quantity` | `int` | Unidades del movimiento. Negativa en salidas y liberaciones. |
| `reason` | `String` | Motivo del movimiento. Obligatorio. |
| `date` | `LocalDateTime` | Fecha y hora del movimiento. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static InventoryMovement restore(UUID id, UUID inventoryId, UUID productId, UUID warehouseId, MovementType type, int quantity, String reason, LocalDateTime date)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Se refiere a un `Inventory`, un `Product` y una `Warehouse`.

## Reglas de negocio

* Es inmutable: una vez creado no se modifica.
* La cantidad no puede ser cero.

---

# Cart

**Área:** Compra

## Descripción

Selección provisional de productos de un comprador. Representa la etapa "Carrito" del ciclo de vida del pedido.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del carrito. |
| `buyer` | `Buyer` | Comprador dueño del carrito. |
| `items` | `List<CartItem>` | Productos seleccionados con su cantidad. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Cart restore(UUID id, Buyer buyer, List<CartItem> items)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void addProduct(Product product, int quantity)` | Agrega un producto publicado; si ya estaba, suma la cantidad. |
| `void removeProduct(UUID productId)` | Quita un producto del carrito. |
| `void clear()` | Vacía el carrito. |

## Relaciones

* Pertenece a un `Buyer`.
* Contiene `CartItem`.
* Al confirmarse da origen a un `Order`.

## Reglas de negocio

* Solo admite productos publicados.
* Agregar al carrito no reserva stock.

---

# CartItem

**Área:** Compra

## Descripción

Línea de un carrito: un producto y la cantidad que el comprador quiere.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `product` | `Product` | Producto seleccionado. |
| `quantity` | `int` | Unidades seleccionadas. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static CartItem restore(Product product, int quantity)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Pertenece a un `Cart`.

## Reglas de negocio

* Solo el `Cart` puede crear o modificar sus líneas.

---

# Order

**Área:** Pedidos

## Descripción

Compromiso comercial formal de un comprador. Su ciclo de vida es el proceso central del sistema (Dominio 7).

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del pedido. |
| `buyer` | `Buyer` | Comprador que realizó el pedido. |
| `items` | `List<OrderItem>` | Líneas del pedido, copiadas del carrito al confirmarlo. |
| `date` | `LocalDateTime` | Fecha y hora de creación. |
| `deliveryAddress` | `Address` | Dirección de entrega que eligió el comprador al confirmar. Si no elige, se usa su dirección principal. |
| `status` | `OrderStatus` | Estado del pedido. Nace `PENDING_PAYMENT`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Order restore(UUID id, Buyer buyer, List<OrderItem> items, LocalDateTime date, Address deliveryAddress, OrderStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `Money calculateTotal()` | Suma los subtotales de todas las líneas. |
| `boolean containsPhysicalProducts()` | Indica si el pedido tiene algún producto físico. |
| `boolean isDigitalOnly()` | Indica si el pedido tiene solo productos digitales. |
| `boolean belongsTo(UUID buyerId)` | Indica si el pedido pertenece al comprador indicado. |
| `void pay()` | Pasa de `PENDING_PAYMENT` a `PAID`. |
| `void ship()` | Pasa de `PAID` a `SHIPPED`. |
| `void complete()` | Pasa de `SHIPPED` a `DELIVERED`. |

## Relaciones

* Pertenece a un `Buyer`.
* Contiene `OrderItem`.
* Puede tener una `Invoice`, un `Shipment`, una `ReturnRequest` y un `Refund`.

## Reglas de negocio

* Los estados avanzan en orden: `PENDING_PAYMENT`, `PAID`, `SHIPPED`, `DELIVERED`.
* Un pedido finalizado no puede modificarse bajo ninguna circunstancia (validación crítica de Pedidos).
* La etapa `CART` del ciclo se representa con la entidad `Cart`.
* La dirección de entrega debe ser una de las direcciones registradas del comprador.

---

# OrderItem

**Área:** Pedidos

## Descripción

Línea de un pedido. Guarda una copia de los datos del producto al momento de la compra, para que un cambio posterior de precio o nombre no altere el pedido.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `productId` | `UUID` | Producto comprado. |
| `productName` | `String` | Nombre del producto al momento de la compra. |
| `type` | `ProductType` | Tipo del producto al momento de la compra. |
| `quantity` | `int` | Unidades compradas. |
| `unitPrice` | `Money` | Precio unitario al momento de la compra. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static OrderItem restore(UUID productId, String productName, ProductType type, int quantity, Money unitPrice)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `Money subtotal()` | Devuelve `unitPrice * quantity`. |
| `boolean isPhysical()` | Indica si el producto comprado es físico. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* Es inmutable.

---

# Invoice

**Área:** Facturación

## Descripción

Información comercial de la venta de un pedido pagado.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único de la factura. |
| `order` | `Order` | Pedido facturado. |
| `total` | `Money` | Total facturado, tomado del pedido. |
| `date` | `LocalDateTime` | Fecha y hora de emisión. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Invoice restore(UUID id, Order order, Money total, LocalDateTime date)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* Solo se factura un pedido pagado.
* El total sale del pedido y no se modifica después de emitida.
* Un pedido tiene como máximo una factura.

---

# Shipment

**Área:** Logística

## Descripción

Proceso logístico de entrega de un pedido con productos físicos.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del envío. |
| `order` | `Order` | Pedido que se envía. |
| `deliveryAddress` | `Address` | Dirección de entrega, que debe ser del comprador. |
| `status` | `ShipmentStatus` | Estado del envío. Nace `PENDING`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Shipment restore(UUID id, Order order, Address deliveryAddress, ShipmentStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void ship()` | Pasa de `PENDING` a `SHIPPED`. |
| `void deliver()` | Pasa de `SHIPPED` a `DELIVERED`. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* Solo se crea para un pedido pagado con productos físicos.
* Su estado avanza junto con el del pedido (lo coordinan `ShipOrderService` y `ConfirmDeliveryService`).

---

# ReturnRequest

**Área:** Posventa

## Descripción

Solicitud de un comprador para devolver un pedido entregado.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único de la devolución. |
| `order` | `Order` | Pedido devuelto. |
| `reason` | `String` | Motivo de la devolución. Obligatorio. |
| `status` | `ReturnStatus` | Estado de la devolución. Nace `REQUESTED`. |
| `restockedUnits` | `Map<UUID, Integer>` | Unidades ya reintegradas al inventario, por producto. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static ReturnRequest restore(UUID id, Order order, String reason, ReturnStatus status, Map<UUID, Integer> restockedUnits)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void approve()` | Aprueba la devolución. |
| `void reject()` | Rechaza la devolución. |
| `boolean isApproved()` | Indica si la devolución fue aprobada. |
| `boolean isOpen()` | Indica si la devolución está solicitada o aprobada. |
| `int purchasedUnits(UUID productId)` | Unidades compradas de un producto en el pedido. |
| `int restockedUnits(UUID productId)` | Unidades de un producto ya reintegradas. |
| `int unitsPendingRestock(UUID productId)` | Unidades de un producto que aún pueden reintegrarse. |
| `void registerRestock(UUID productId, int units)` | Registra unidades reintegradas, sin superar las compradas. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* Solo se devuelve un pedido entregado.
* Se resuelve una sola vez (aprobada o rechazada).
* Las unidades de una devolución se reintegran una sola vez.

---

# Refund

**Área:** Posventa

## Descripción

Devolución del dinero de un pedido pagado.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del reembolso. |
| `order` | `Order` | Pedido reembolsado. |
| `amount` | `Money` | Monto a reembolsar. |
| `status` | `RefundStatus` | Estado del reembolso. Nace `PENDING`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Refund restore(UUID id, Order order, Money amount, RefundStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void process()` | Marca el reembolso como procesado. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* El monto es mayor que cero, en la moneda del pedido y no supera su total.
* Se procesa una sola vez.

---

# AuditLog

**Área:** Auditoría

## Descripción

Registro inmutable de una operación del negocio: quién la hizo, con qué rol, cuándo y sobre qué registro. Se guarda en MongoDB, igual que la auditoría del proyecto de referencia, y da la trazabilidad que pide la especificación.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del registro. |
| `operation` | `AuditOperation` | Operación auditada. |
| `date` | `LocalDateTime` | Fecha y hora de la operación. |
| `performedByUserId` | `UUID` | Usuario que hizo la operación. |
| `performedByRole` | `UserRole` | Rol del usuario en ese momento. |
| `affectedEntityId` | `UUID` | Registro afectado (pedido, producto, inventario, usuario, etc.). |
| `details` | `Map<String, String>` | Datos adicionales de la operación, como el nuevo estado o el monto. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static AuditLog restore(UUID id, AuditOperation operation, LocalDateTime date, UUID performedByUserId, UserRole performedByRole, UUID affectedEntityId, Map<String, String> details)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Referencia a un `User` por su id.
* Referencia al registro afectado por su id, sin depender de su tipo.

## Reglas de negocio

* Es inmutable: el registro solo crece, nunca se modifica ni se borra.
* Exige la operación, el usuario y el registro afectado.
* Guarda una copia de los detalles, así que cambiarlos después no altera el registro.

---

# Decisiones de diseño y supuestos

La especificación funcional no define algunos casos. Se resolvieron así:

* **Bodega digital.** La especificación solo habla de bodegas físicas. La bodega digital, sin dirección, es una extensión del modelo pensada para los productos digitales.
* **Productos digitales.** No usan inventario ni envío, porque se entregan de inmediato tras el pago.
* **Pedidos mixtos.** Un pedido con productos físicos y digitales sigue el flujo de logística completo, porque el estado del pedido es uno solo.
* **Devoluciones.** Las solicita el comprador y las resuelve el administrador. Cubren el pedido completo, así que el reembolso es por el total.
* **Gestión de devoluciones.** La matriz de responsabilidades no la incluye; se asignó a comprador y administrador, igual que los reembolsos.
* **Unicidad de nombres.** El nombre de una bodega es único en la plataforma; el de un producto es único dentro de cada vendedor.
* **Perfiles.** `Buyer` y `Seller` se relacionan con `User` por `userId` en lugar de heredar de él, porque cada usuario tiene un único rol (RG-02) y su perfil guarda datos propios del negocio.
* **Reconstrucción desde la base de datos.** Los constructores públicos crean objetos nuevos: generan el id y fijan el estado inicial. Para leer de MySQL cada entidad tiene un método estático `restore(...)` que recibe el id y el estado guardados. Solo lo usan los adaptadores de persistencia, y no vuelve a ejecutar efectos de creación (por ejemplo, restaurar una reserva no aparta unidades otra vez).
'@

Escribir "..\SDD\domain\Domain Services.md" @'
# Domain Services

## Introducción

Los servicios de dominio contienen las reglas de negocio que no caben en una sola entidad, por ejemplo repartir la reserva de un pedido entre varias bodegas o despachar un pedido actualizando a la vez el envío, el pedido y el inventario. No dependen de bases de datos, frameworks ni HTTP: reciben entidades del dominio y, cuando necesitan información externa, usan un puerto de salida.

Hay 34 servicios en `domain/services/`, organizados por subdominio. El detalle de cada uno (operaciones, dependencias, rol que lo ejecuta y reglas) está en la carpeta `services/`.

Las operaciones que modifican varias entidades validan todo antes de cambiar algo, y las que reservan stock son de todo o nada: si algo falla a mitad de camino, se deshace lo que ya se había hecho.

---

# Autorización y validación

Servicios de apoyo que deciden quién puede ejecutar cada operación. Los demás servicios los usan antes de cambiar cualquier entidad. Aplican RG-01, RG-02, RG-03 y la matriz de responsabilidades (sección 12).

Detalle: `services/authorization-services.md`

## AuthorizationService

Decide si un usuario puede ejecutar un proceso de negocio según su rol y su estado.

## BuyerValidationService

Comprueba que un usuario pueda comprar con un perfil de comprador.

## InventoryAccessService

Decide quién puede administrar un inventario.

## SellerValidationService

Comprueba que un usuario actúe como vendedor y, cuando aplica, que sea dueño del producto.

---

# Usuarios, vendedores y bodegas

Registro de usuarios y de sus perfiles, incorporación de vendedores y administración de bodegas (Dominios 1 a 4).

Detalle: `services/user-services.md`

## OnboardSellerService

Incorpora a un vendedor junto con su primera bodega, como describe el flujo 6.1.

## RegisterBuyerService

Crea el perfil de comprador de un usuario.

## RegisterSellerService

Crea el perfil de vendedor de un usuario.

## RegisterUserService

Registra un usuario nuevo.

## RegisterWarehouseService

Registra bodegas del Marketplace o de un vendedor.

---

# Catálogo

Registro y ciclo de vida de los productos (Dominio 5).

Detalle: `services/catalog-services.md`

## ChangeProductStatusService

Suspende o descontinúa un producto.

## CheckAvailabilityService

Calcula cuántas unidades de un producto hay disponibles entre todas las bodegas.

## PublishProductService

Publica un producto en el catálogo.

## RegisterProductService

Registra un producto nuevo de un vendedor.

---

# Inventario

Existencias distribuidas por bodega y sus cinco movimientos: ingreso, reserva, salida por venta, ajuste y devolución (Dominio 6).

Detalle: `services/inventory-services.md`

## AdjustInventoryService

Ajusta las existencias de un inventario con un motivo.

## InventoryOutflowService

Convierte las reservas de un pedido en salida definitiva.

## MarkInventoryService

Marca un inventario como dañado, lo bloquea o lo habilita.

## RegisterInitialInventoryService

Registra las existencias iniciales de un producto en una bodega.

## ReleaseReservationService

Deshace las reservas de un pedido que no se concreta.

## ReserveInventoryService

Aparta stock de un producto para un pedido, repartiéndolo entre varias bodegas si hace falta.

## RestockInventoryService

Reintegra al inventario las unidades de una devolución aprobada.

---

# Carrito y pedidos

Desde la selección de productos hasta el pago y la entrega de productos digitales (OBJ-07, OBJ-08 y Dominio 7).

Detalle: `services/order-services.md`

## AddToCartService

Agrega un producto al carrito del comprador.

## ConfirmOrderService

Convierte el carrito en un pedido y reserva el stock de cada producto.

## DeliverDigitalOrderService

Entrega de inmediato un pedido pagado formado solo por productos digitales.

## PayOrderService

Registra el pago de un pedido.

---

# Facturación y logística

Factura, envío, despacho y confirmación de entrega (OBJ-09 y OBJ-10).

Detalle: `services/logistics-billing-services.md`

## ConfirmDeliveryService

Confirma la entrega y finaliza el pedido.

## CreateShipmentService

Crea el envío de un pedido pagado con productos físicos.

## IssueInvoiceService

Emite la factura de un pedido pagado.

## ShipOrderService

Despacha un pedido: confirma la salida de inventario y actualiza envío y pedido a la vez.

---

# Posventa

Devoluciones y reembolsos (OBJ-11).

Detalle: `services/after-sales-services.md`

## GenerateRefundService

Genera el reembolso de una devolución aprobada.

## ProcessRefundService

Procesa un reembolso pendiente.

## RequestReturnService

Registra la solicitud de devolución de un pedido.

## ResolveReturnService

Aprueba o rechaza una devolución.

---

# Auditoría

Registro y consulta de la auditoría de operaciones, guardada en MongoDB (trazabilidad y OBJ-12).

Detalle: `services/audit-services.md`

## ConsultAuditLogService

Consulta la auditoría completa o la de un registro específico.

## RegisterAuditLogService

Deja el registro de auditoría de una operación que ya terminó con éxito.
'@

Escribir "..\SDD\domain\Domain Value Objects.md" @'
# Domain Value Objects

## Introducción

Los objetos de valor representan conceptos del negocio que no tienen identidad propia: importan por su contenido. Son inmutables y dos objetos con los mismos datos se consideran iguales. Cada uno valida sus reglas al crearse, así que dentro del dominio nunca existe un correo mal escrito ni un monto negativo.

En el código se implementan como `record` de Java en `domain/valueobjects/`. Los estados, roles y tipos acotados del negocio se implementan como `enum` en `domain/enums/`, de modo que ningún campo acotado queda como `String` abierto.

---

# Jerarquía

```text
valueobjects
├── Address
├── Email
├── FullName
├── IdentityDocument
└── Money

enums
├── AuditOperation
├── BusinessProcess
├── CommercialStatus
├── InventoryStatus
├── MovementType
├── OrderStatus
├── ProductStatus
├── ProductType
├── RefundStatus
├── ReservationStatus
├── ReturnStatus
├── ShipmentStatus
├── UserRole
├── UserStatus
├── WarehouseOwnership
└── WarehouseType
```

---

# Objetos de valor

---

# Address

## Descripción

Ubicación física: direcciones del comprador, bodegas físicas y destino de los envíos.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `street` | `String` | Calle y número. |
| `city` | `String` | Ciudad. |
| `department` | `String` | Departamento. |
| `postalCode` | `String` | Código postal. |

## Reglas

* Todos los campos son obligatorios.

---

# Email

## Descripción

Correo electrónico de un usuario.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `value` | `String` | Dirección de correo. Se guarda en minúsculas y sin espacios. |

## Reglas

* No puede estar vacío.
* Debe tener un formato válido (`usuario@dominio.ext`).
* "Ana@Mail.com" y "ana@mail.com" son el mismo correo.

---

# FullName

## Descripción

Nombre de una persona registrada.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `firstNames` | `String` | Nombres. |
| `lastNames` | `String` | Apellidos. |

## Reglas

* Ambos campos son obligatorios.

---

# IdentityDocument

## Descripción

Documento de identidad de un usuario.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `value` | `String` | Número del documento. |

## Reglas

* No puede estar vacío.
* Solo admite dígitos.

---

# Money

## Descripción

Valor monetario: precios, subtotales, totales, pagos y reembolsos.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `amount` | `BigDecimal` | Monto, siempre con dos decimales. |
| `currency` | `String` | Código de moneda. `COP` por defecto. |

## Reglas

* El monto no puede ser negativo.
* La moneda es obligatoria y se guarda en mayúsculas.
* Fijar dos decimales permite comparar montos iguales sin errores de redondeo.

---

# Enumeraciones

---

# AuditOperation

## Descripción

Operaciones del negocio que dejan registro en la auditoría.

## Valores permitidos

| Valor | Significado |
|---|---|
| `USER_REGISTRATION` | Registro de un usuario. |
| `USER_BLOCKING` | Bloqueo de un usuario. |
| `USER_ACTIVATION` | Reactivación de un usuario. |
| `SELLER_ONBOARDING` | Alta de un vendedor con su primera bodega. |
| `WAREHOUSE_REGISTRATION` | Registro de una bodega. |
| `PRODUCT_REGISTRATION` | Registro de un producto. |
| `PRODUCT_STATUS_CHANGE` | Publicación, suspensión o descontinuación de un producto. |
| `INVENTORY_REGISTRATION` | Registro de inventario inicial. |
| `INVENTORY_ADJUSTMENT` | Ajuste de existencias. |
| `INVENTORY_STATUS_CHANGE` | Inventario marcado como dañado, bloqueado o habilitado. |
| `INVENTORY_RESTOCK` | Reintegro de unidades por devolución. |
| `ORDER_CONFIRMATION` | Confirmación de un pedido. |
| `ORDER_PAYMENT` | Pago de un pedido. |
| `ORDER_SHIPMENT` | Despacho de un pedido. |
| `ORDER_DELIVERY` | Entrega de un pedido. |
| `RETURN_REQUEST` | Solicitud de devolución. |
| `RETURN_APPROVAL` | Aprobación de una devolución. |
| `RETURN_REJECTION` | Rechazo de una devolución. |
| `REFUND_PROCESSING` | Procesamiento de un reembolso. |

---

# BusinessProcess

## Descripción

Procesos de negocio que controla `AuthorizationService`.

## Valores permitidos

| Valor | Significado |
|---|---|
| `SELLER_REGISTRATION` | Registro de vendedores: administrador. |
| `PRODUCT_REGISTRATION` | Registro de productos: vendedor. |
| `INVENTORY_MANAGEMENT` | Administración de inventario: vendedor y operador logístico. |
| `ORDER_MANAGEMENT` | Gestión de pedidos: comprador, vendedor y operador logístico. |
| `REFUND_MANAGEMENT` | Gestión de reembolsos: comprador y administrador. |
| `RETURN_MANAGEMENT` | Gestión de devoluciones: comprador y administrador (supuesto). |
| `WAREHOUSE_MANAGEMENT` | Administración de bodegas: administrador. |
| `SHIPPING_OPERATIONS` | Operación de despachos: operador logístico. |
| `USER_MANAGEMENT` | Administración de usuarios (bloquear y reactivar): administrador. |
| `ADMINISTRATIVE_REPORTS` | Consultas administrativas de seguimiento (OBJ-12): administrador y supervisor. |

---

# CommercialStatus

## Descripción

Condición del comprador para realizar compras.

## Valores permitidos

| Valor | Significado |
|---|---|
| `ENABLED` | Puede comprar. |
| `RESTRICTED` | No puede comprar. |

---

# InventoryStatus

## Descripción

Estado de un inventario.

## Valores permitidos

| Valor | Significado |
|---|---|
| `AVAILABLE` | Se puede reservar. |
| `DAMAGED` | No se puede reservar ni reintegrar. |
| `BLOCKED` | Detenido temporalmente. |

---

# MovementType

## Descripción

Tipo de movimiento de inventario (Dominio 6).

## Valores permitidos

| Valor | Significado |
|---|---|
| `INFLOW` | Ingreso. |
| `RESERVATION` | Reserva o liberación de reserva. |
| `SALE` | Salida por venta. |
| `ADJUSTMENT` | Ajuste. |
| `RETURN` | Devolución. |

---

# OrderStatus

## Descripción

Ciclo de estados del pedido (Dominio 7).

## Valores permitidos

| Valor | Significado |
|---|---|
| `CART` | Selección provisional; se representa con la entidad `Cart`. |
| `PENDING_PAYMENT` | Espera de confirmación financiera. |
| `PAID` | Inicio del alistamiento. |
| `SHIPPED` | Salida física de la bodega. |
| `DELIVERED` | Entregado y finalizado; ya no se modifica. |

---

# ProductStatus

## Descripción

Estado de publicación de un producto.

## Valores permitidos

| Valor | Significado |
|---|---|
| `PUBLISHED` | Visible en el catálogo. |
| `SUSPENDED` | No visible; puede volver a publicarse. |
| `DISCONTINUED` | Retirado de forma definitiva. |

---

# ProductType

## Descripción

Tipo de producto (Dominio 5).

## Valores permitidos

| Valor | Significado |
|---|---|
| `PHYSICAL` | Requiere inventario y despacho. |
| `DIGITAL` | Se entrega de inmediato tras el pago. |

---

# RefundStatus

## Descripción

Estado de un reembolso.

## Valores permitidos

| Valor | Significado |
|---|---|
| `PENDING` | Por procesar. |
| `PROCESSED` | Procesado. |

---

# ReservationStatus

## Descripción

Estado de una reserva de inventario.

## Valores permitidos

| Valor | Significado |
|---|---|
| `ACTIVE` | Unidades apartadas. |
| `CONFIRMED` | Convertida en salida por venta. |
| `RELEASED` | Unidades devueltas al disponible. |

---

# ReturnStatus

## Descripción

Estado de una devolución.

## Valores permitidos

| Valor | Significado |
|---|---|
| `REQUESTED` | Solicitada por el comprador. |
| `APPROVED` | Aprobada por el administrador. |
| `REJECTED` | Rechazada por el administrador. |

---

# ShipmentStatus

## Descripción

Estado de un envío.

## Valores permitidos

| Valor | Significado |
|---|---|
| `PENDING` | Creado, aún sin despachar. |
| `SHIPPED` | Despachado. |
| `DELIVERED` | Entregado. |

---

# UserRole

## Descripción

Rol único de un usuario (RG-02).

## Valores permitidos

| Valor | Significado |
|---|---|
| `BUYER` | Comprador. |
| `SELLER` | Vendedor. |
| `LOGISTIC_OPERATOR` | Operador logístico. |
| `ADMIN` | Administrador. |
| `SUPERVISOR` | Supervisor (consulta y seguimiento). |

---

# UserStatus

## Descripción

Condición operativa de un usuario.

## Valores permitidos

| Valor | Significado |
|---|---|
| `ACTIVE` | Puede operar. |
| `BLOCKED` | No puede ejecutar operaciones. |

---

# WarehouseOwnership

## Descripción

Clasificación de la bodega según su dueño (Dominio 4).

## Valores permitidos

| Valor | Significado |
|---|---|
| `MARKETPLACE` | Bodega del Marketplace. |
| `SELLER` | Bodega de un vendedor. |

---

# WarehouseType

## Descripción

Tipo de bodega.

## Valores permitidos

| Valor | Significado |
|---|---|
| `PHYSICAL` | Espacio físico, con dirección. |
| `DIGITAL` | Extensión del modelo, sin dirección física. |
'@

Escribir "..\SDD\domain\Input-ports.md" @'
# Input Ports

## Introducción

Los puertos de entrada son los contratos con los que el exterior (por ejemplo, un controlador REST) usa el dominio. Están en `domain/ports/in/` y se organizan por rol: un puerto por cada rol de `UserRole`, más un puerto de acceso público para lo que no requiere iniciar sesión.

Así cada rol ve solo las operaciones que tiene permitidas, y la capa REST y la seguridad pueden validar el rol contra el puerto que se invoca.

---

# Principios

* **Aislamiento por rol.** Cada puerto agrupa las operaciones de un solo rol. Si dos roles comparten una operación (por ejemplo, el inventario del vendedor y del operador logístico), aparece en ambos puertos.
* **Parámetros del dominio.** Los métodos reciben entidades y objetos de valor del dominio, nunca DTOs. El primer parámetro es el `User` que hace la petición; en la etapa de seguridad se reconstruirá a partir del token JWT.
* **Implementación en adaptadores.** Cada puerto lo implementa una clase de `adapters/useCases/` (ver `../Adapters/Use-cases-adapters.md`). Los servicios de dominio ya no implementan puertos: son las piezas que esas clases inyectan.

---

# Definición de los puertos

## 1. PublicAccessPort

Operaciones disponibles sin iniciar sesión: el registro de compradores y la consulta del catálogo.

**Rol:** Público (sin autenticación)  
**Implementado por:** `PublicAccessUseCaseImpl`

```java
public interface PublicAccessPort {
    Buyer registerBuyer(User user, Address mainAddress);
    List<Product> consultCatalog();
    int consultAvailability(Product product);
}
```

## 2. BuyerPort

Carrito, confirmación y pago de pedidos, consulta de sus pedidos y solicitud de devoluciones.

**Rol:** `BUYER`  
**Implementado por:** `BuyerUseCaseImpl`

```java
public interface BuyerPort {
    Cart consultMyCart(User user);
    Cart addToCart(User user, Product product, int quantity);
    Cart removeFromCart(User user, Product product);
    Order confirmOrder(User user, Address deliveryAddress);
    Order payOrder(User user, Order order, Money paidAmount);
    List<Order> consultMyOrders(User user);
    ReturnRequest requestReturn(User user, Order order, String reason);
}
```

## 3. SellerPort

Catálogo e inventario de los productos propios del vendedor.

**Rol:** `SELLER`  
**Implementado por:** `SellerUseCaseImpl`

```java
public interface SellerPort {
    Product registerProduct(User user, Product product);
    Product publishProduct(User user, Product product);
    Product suspendProduct(User user, Product product);
    Product discontinueProduct(User user, Product product);
    List<Product> consultMyProducts(User user);
    Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity);
    Inventory adjustInventory(User user, Inventory inventory, int difference, String reason);
    Inventory markInventoryDamaged(User user, Inventory inventory);
    Inventory blockInventory(User user, Inventory inventory);
    Inventory enableInventory(User user, Inventory inventory);
    Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units);
}
```

## 4. LogisticOperatorPort

Operación de bodegas (inventario de cualquier vendedor) y despacho y entrega de pedidos.

**Rol:** `LOGISTIC_OPERATOR`  
**Implementado por:** `LogisticOperatorUseCaseImpl`

```java
public interface LogisticOperatorPort {
    Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity);
    Inventory adjustInventory(User user, Inventory inventory, int difference, String reason);
    Inventory markInventoryDamaged(User user, Inventory inventory);
    Inventory blockInventory(User user, Inventory inventory);
    Inventory enableInventory(User user, Inventory inventory);
    Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units);
    List<Order> consultOrdersToShip(User user);
    Order shipOrder(User user, Order order);
    Order confirmDelivery(User user, Order order);
}
```

## 5. AdminPort

Usuarios, alta de vendedores, bodegas y decisiones de posventa (devoluciones y reembolsos).

**Rol:** `ADMIN`  
**Implementado por:** `AdminUseCaseImpl`

```java
public interface AdminPort {
    User registerUser(User admin, User newUser);
    User blockUser(User admin, User target);
    User activateUser(User admin, User target);
    Seller onboardSeller(User admin, User sellerUser, Warehouse firstWarehouse);
    Warehouse registerMarketplaceWarehouse(User admin, Warehouse warehouse);
    Warehouse registerSellerWarehouse(User admin, Seller seller, Warehouse warehouse);
    ReturnRequest approveReturn(User admin, ReturnRequest returnRequest);
    ReturnRequest rejectReturn(User admin, ReturnRequest returnRequest);
    Refund processRefund(User admin, Refund refund);
    List<AuditLog> consultAuditLog(User admin);
}
```

## 6. SupervisorPort

Consultas de seguimiento sin modificar datos (OBJ-12).

**Rol:** `SUPERVISOR`  
**Implementado por:** `SupervisorUseCaseImpl`

```java
public interface SupervisorPort {
    List<User> consultUsers(User supervisor);
    List<Warehouse> consultWarehouses(User supervisor);
    List<Inventory> consultInventory(User supervisor);
    List<Order> consultOrders(User supervisor);
    List<AuditLog> consultAuditLog(User supervisor);
    List<AuditLog> consultAuditLogOf(User supervisor, UUID entityId);
}
```
'@

Escribir "..\SDD\domain\Output-ports.md" @'
# Output Ports

## Introducción

Los puertos de salida son los contratos con los que el dominio pide información o servicios externos. El dominio es dueño de estas interfaces y nunca depende directamente de bases de datos, JPA, Spring ni HTTP. Sus implementaciones pertenecen a la capa de adaptadores de persistencia.

```text
Use Case Adapter / Domain Service
      |
      v
Output Port  (domain/ports/out)
      |
      v
Persistence Adapter  (adapters/persistence, Fase B: MySQL)
      |
      v
Base de datos
```

Hay 15 puertos, uno por entidad que se guarda. Siguiendo el proyecto de referencia, la auditoría (`AuditLogRepositoryPort`) va en MongoDB y el resto en MySQL. Las búsquedas devuelven `Optional` cuando puede no haber resultado y `List` cuando puede haber varios. En las pruebas se usan implementaciones en memoria (`InMemoryRepositories`).

---

## AuditLogRepositoryPort

Auditoría de operaciones. Es el único puerto implementado con **MongoDB**; los demás usan MySQL.

**Lo usan:** `ConsultAuditLogService`, `RegisterAuditLogService`

```java
void save(AuditLog auditLog);
List<AuditLog> findAll();
List<AuditLog> findByUser(UUID userId);
List<AuditLog> findByAffectedEntity(UUID entityId);
```

## BuyerRepositoryPort

Perfiles de comprador, buscados por id o por el usuario al que pertenecen.

**Lo usan:** `BuyerUseCaseImpl`, `RegisterBuyerService`

```java
boolean existsByUserId(UUID userId);
Optional<Buyer> findById(UUID id);
Optional<Buyer> findByUserId(UUID userId);
void save(Buyer buyer);
```

## CartRepositoryPort

Carrito activo de cada comprador.

**Lo usan:** `BuyerUseCaseImpl`

```java
Optional<Cart> findByBuyer(UUID buyerId);
void save(Cart cart);
```

## InventoryMovementRepositoryPort

Historial de movimientos de inventario (trazabilidad, RN-04).

**Lo usan:** `BuyerUseCaseImpl`, `InventoryOperationsSupport`, `LogisticOperatorUseCaseImpl`

```java
List<InventoryMovement> findByInventory(UUID inventoryId);
void saveAll(List<InventoryMovement> movements);
```

## InventoryRepositoryPort

Inventario por producto y bodega.

**Lo usan:** `BuyerUseCaseImpl`, `InventoryOperationsSupport`, `LogisticOperatorUseCaseImpl`, `PublicAccessUseCaseImpl`, `SellerUseCaseImpl`, `SupervisorUseCaseImpl`

```java
Optional<Inventory> findById(UUID id);
List<Inventory> findByProduct(UUID productId);
List<Inventory> findAll();
void save(Inventory inventory);
void saveAll(List<Inventory> inventories);
```

## InventoryReservationRepositoryPort

Reservas de stock de cada pedido.

**Lo usan:** `BuyerUseCaseImpl`, `LogisticOperatorUseCaseImpl`

```java
List<InventoryReservation> findByOrder(UUID orderId);
void saveAll(List<InventoryReservation> reservations);
```

## InvoiceRepositoryPort

Factura de cada pedido.

**Lo usan:** `BuyerUseCaseImpl`

```java
Optional<Invoice> findByOrder(UUID orderId);
void save(Invoice invoice);
```

## OrderRepositoryPort

Pedidos, por id, por comprador o todos.

**Lo usan:** `BuyerUseCaseImpl`, `LogisticOperatorUseCaseImpl`, `SupervisorUseCaseImpl`

```java
Optional<Order> findById(UUID id);
List<Order> findByBuyer(UUID buyerId);
List<Order> findAll();
void save(Order order);
```

## ProductRepositoryPort

Productos: unicidad del nombre por vendedor, productos de un vendedor y catálogo publicado.

**Lo usan:** `BuyerUseCaseImpl`, `InventoryOperationsSupport`, `PublicAccessUseCaseImpl`, `RegisterProductService`, `SellerUseCaseImpl`

```java
boolean existsNameForSeller(UUID sellerId, String name);
Optional<Product> findById(UUID id);
List<Product> findBySeller(UUID sellerId);
List<Product> findPublished();
void save(Product product);
```

## RefundRepositoryPort

Reembolsos, por id o por pedido.

**Lo usan:** `AdminUseCaseImpl`

```java
Optional<Refund> findById(UUID id);
Optional<Refund> findByOrder(UUID orderId);
void save(Refund refund);
```

## ReturnRequestRepositoryPort

Solicitudes de devolución de cada pedido.

**Lo usan:** `AdminUseCaseImpl`, `BuyerUseCaseImpl`, `InventoryOperationsSupport`

```java
Optional<ReturnRequest> findById(UUID id);
List<ReturnRequest> findByOrder(UUID orderId);
void save(ReturnRequest returnRequest);
```

## SellerRepositoryPort

Perfiles de vendedor, buscados por id o por el usuario al que pertenecen.

**Lo usan:** `AdminUseCaseImpl`, `RegisterSellerService`, `SellerUseCaseImpl`

```java
boolean existsByUserId(UUID userId);
Optional<Seller> findById(UUID id);
Optional<Seller> findByUserId(UUID userId);
void save(Seller seller);
```

## ShipmentRepositoryPort

Envío de cada pedido.

**Lo usan:** `BuyerUseCaseImpl`, `LogisticOperatorUseCaseImpl`

```java
Optional<Shipment> findByOrder(UUID orderId);
void save(Shipment shipment);
```

## UserRepositoryPort

Usuarios de la plataforma: unicidad de correo y documento, búsqueda y guardado.

**Lo usan:** `AdminUseCaseImpl`, `RegisterUserService`, `SupervisorUseCaseImpl`

```java
boolean existsByEmail(Email email);
boolean existsByDocument(IdentityDocument document);
Optional<User> findById(UUID id);
Optional<User> findByEmail(Email email);
List<User> findAll();
void save(User user);
```

## WarehouseRepositoryPort

Bodegas del marketplace y de los vendedores; el nombre es único.

**Lo usan:** `AdminUseCaseImpl`, `InventoryOperationsSupport`, `RegisterWarehouseService`, `SupervisorUseCaseImpl`

```java
boolean existsByName(String name);
Optional<Warehouse> findById(UUID id);
List<Warehouse> findAll();
void save(Warehouse warehouse);
```
'@

Escribir "..\SDD\domain\services\audit-services.md" @'
# Auditoría

Registro y consulta de la auditoría de operaciones, guardada en MongoDB (trazabilidad y OBJ-12).

---

## ConsultAuditLogService

**Propósito:** Consulta la auditoría completa o la de un registro específico.

**Quién lo ejecuta:** Administrador y supervisor.

**Lo usa:** `AdminUseCaseImpl`, `SupervisorUseCaseImpl`

**Dependencias:** `AuthorizationService authorization`, `AuditLogRepositoryPort auditLogs`

**Operaciones:**

```java
List<AuditLog> consultAll(User actor);
List<AuditLog> consultByEntity(User actor, UUID entityId);
```

**Reglas de negocio:**

* Exige `ADMINISTRATIVE_REPORTS`.
* Devuelve los registros del más reciente al más antiguo; si dos tienen la misma hora, primero el último guardado.

---

## RegisterAuditLogService

**Propósito:** Deja el registro de auditoría de una operación que ya terminó con éxito.

**Quién lo ejecuta:** Interno (lo usan los casos de uso después de cada operación).

**Lo usa:** `AdminUseCaseImpl`, `BuyerUseCaseImpl`, `InventoryOperationsSupport`, `LogisticOperatorUseCaseImpl`, `PublicAccessUseCaseImpl`, `SellerUseCaseImpl`

**Dependencias:** `AuditLogRepositoryPort auditLogs`

**Operaciones:**

```java
AuditLog register(AuditOperation operation, User performedBy, UUID affectedEntityId, Map<String, String> details);
```

**Reglas de negocio:**

* Exige la operación, el usuario que la hizo y el registro afectado.
* Solo se llama cuando la operación ya se completó: una operación que falla no deja registro.
* Los registros no se modifican ni se borran.
'@

Write-Host "Listo: $creados archivos." -ForegroundColor Cyan
