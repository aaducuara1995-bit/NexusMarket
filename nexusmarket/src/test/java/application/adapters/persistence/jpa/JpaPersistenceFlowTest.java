package application.adapters.persistence.jpa;

import application.domain.entities.Buyer;
import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.AuditOperation;
import application.domain.enums.MovementType;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReservationStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.enums.WarehouseType;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the business flow through the JPA adapters. Every step reads back what the
 * previous one saved, so it proves that mappers and adapters keep ids, states and
 * references intact between operations.
 */
class JpaPersistenceFlowTest {

    private int sequence = 0;

    private User newUser(UserRole role) {
        sequence++;
        return new User(new FullName("Persona", "Numero" + sequence), new Email("p" + sequence + "@mail.com"),
                new IdentityDocument("90" + sequence), role);
    }

    private static Address address(String street) {
        return new Address(street, "Medellin", "Antioquia", "050001");
    }

    @Test
    void aPhysicalPurchaseSurvivesEveryRoundTripToTheDatabase() {
        JpaTestContext ctx = new JpaTestContext();
        User admin = newUser(UserRole.ADMIN);
        ctx.users.save(admin);

        User sellerUser = ctx.adminUseCase.registerUser(admin, newUser(UserRole.SELLER));
        ctx.adminUseCase.onboardSeller(admin, sellerUser,
                Warehouse.ofMarketplace("Bodega Norte", WarehouseType.PHYSICAL, address("Bodega 1")));
        Warehouse warehouse = ctx.warehouses.findAll().get(0);

        Product draft = new Product(UUID.randomUUID(), "Camiseta", "Algodon", ProductType.PHYSICAL,
                new Money(new BigDecimal("45000")));
        draft.addVariant("M");
        Product product = ctx.sellerUseCase.registerProduct(sellerUser, draft);
        ctx.sellerUseCase.registerInitialInventory(sellerUser, product, warehouse, 10);
        ctx.sellerUseCase.publishProduct(sellerUser, product);
        assertEquals(1, ctx.publicAccess.consultCatalog().size());
        assertEquals(java.util.List.of("M"), ctx.products.findById(product.getId()).orElseThrow().getVariants());

        Buyer buyer = ctx.publicAccess.registerBuyer(newUser(UserRole.BUYER), address("Calle 10"));
        User buyerUser = ctx.users.findById(buyer.getUserId()).orElseThrow();
        Address office = address("Oficina 404");
        Buyer storedBuyer = ctx.buyers.findById(buyer.getId()).orElseThrow();
        storedBuyer.addAdditionalAddress(office);
        ctx.buyers.save(storedBuyer);

        ctx.buyerUseCase.addToCart(buyerUser, product, 3);
        assertEquals(3, ctx.buyerUseCase.consultMyCart(buyerUser).getItems().get(0).getQuantity());

        Order order = ctx.buyerUseCase.confirmOrder(buyerUser, office);
        assertEquals(7, ctx.publicAccess.consultAvailability(product));
        assertEquals(3, ctx.inventories.findByProduct(product.getId()).get(0).getReserved());
        assertTrue(ctx.buyerUseCase.consultMyCart(buyerUser).getItems().isEmpty());

        ctx.buyerUseCase.payOrder(buyerUser, order, new Money(new BigDecimal("135000")));
        Order paid = ctx.orders.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, paid.getStatus());
        assertEquals(office, paid.getDeliveryAddress());
        assertEquals(new Money(new BigDecimal("135000")),
                ctx.invoices.findByOrder(order.getId()).orElseThrow().getTotal());
        assertEquals(office, ctx.shipments.findByOrder(order.getId()).orElseThrow().getDeliveryAddress());

        User operator = ctx.adminUseCase.registerUser(admin, newUser(UserRole.LOGISTIC_OPERATOR));
        assertEquals(1, ctx.logisticUseCase.consultOrdersToShip(operator).size());
        ctx.logisticUseCase.shipOrder(operator, order);
        Inventory afterShipping = ctx.inventories.findByProduct(product.getId()).get(0);
        assertEquals(7, afterShipping.getQuantity());
        assertEquals(0, afterShipping.getReserved());
        assertTrue(ctx.reservations.findByOrder(order.getId()).stream()
                .allMatch(r -> r.getStatus() == ReservationStatus.CONFIRMED));
        assertTrue(ctx.movements.findByInventory(afterShipping.getId()).stream()
                .anyMatch(m -> m.getType() == MovementType.SALE && m.getQuantity() == -3));

        ctx.logisticUseCase.confirmDelivery(operator, order);
        assertEquals(OrderStatus.DELIVERED, ctx.orders.findById(order.getId()).orElseThrow().getStatus());
        assertEquals(ShipmentStatus.DELIVERED, ctx.shipments.findByOrder(order.getId()).orElseThrow().getStatus());

        ReturnRequest returnRequest = ctx.buyerUseCase.requestReturn(buyerUser, order, "Talla equivocada");
        ctx.adminUseCase.approveReturn(admin, returnRequest);
        Refund refund = ctx.refunds.findByOrder(order.getId()).orElseThrow();
        assertEquals(RefundStatus.PENDING, refund.getStatus());
        ctx.adminUseCase.processRefund(admin, refund);
        assertEquals(RefundStatus.PROCESSED, ctx.refunds.findById(refund.getId()).orElseThrow().getStatus());

        ctx.sellerUseCase.restockFromReturn(sellerUser, afterShipping, returnRequest, 2);
        assertEquals(9, ctx.inventories.findByProduct(product.getId()).get(0).getQuantity());
        assertEquals(1, ctx.returns.findById(returnRequest.getId()).orElseThrow()
                .unitsPendingRestock(product.getId()));
    }

    @Test
    void aDigitalPurchaseIsDeliveredWithoutShipment() {
        JpaTestContext ctx = new JpaTestContext();
        User admin = newUser(UserRole.ADMIN);
        ctx.users.save(admin);
        User sellerUser = ctx.adminUseCase.registerUser(admin, newUser(UserRole.SELLER));
        ctx.adminUseCase.onboardSeller(admin, sellerUser,
                Warehouse.ofMarketplace("Bodega Digital", WarehouseType.DIGITAL, null));
        assertNull(ctx.warehouses.findAll().get(0).getAddress());

        Product ebook = ctx.sellerUseCase.registerProduct(sellerUser, new Product(UUID.randomUUID(), "Ebook",
                null, ProductType.DIGITAL, new Money(new BigDecimal("20000"))));
        ctx.sellerUseCase.publishProduct(sellerUser, ebook);
        Buyer buyer = ctx.publicAccess.registerBuyer(newUser(UserRole.BUYER), address("Calle 5"));
        User buyerUser = ctx.users.findById(buyer.getUserId()).orElseThrow();

        ctx.buyerUseCase.addToCart(buyerUser, ebook, 1);
        Order order = ctx.buyerUseCase.confirmOrder(buyerUser, null);
        ctx.buyerUseCase.payOrder(buyerUser, order, order.calculateTotal());

        assertEquals(OrderStatus.DELIVERED, ctx.orders.findById(order.getId()).orElseThrow().getStatus());
        assertTrue(ctx.shipments.findByOrder(order.getId()).isEmpty());
        assertTrue(ctx.invoices.findByOrder(order.getId()).isPresent());
    }

    @Test
    void uniquenessChecksWorkAgainstTheStoredRows() {
        JpaTestContext ctx = new JpaTestContext();
        User admin = newUser(UserRole.ADMIN);
        ctx.users.save(admin);
        User sellerUser = ctx.adminUseCase.registerUser(admin, newUser(UserRole.SELLER));

        assertTrue(ctx.users.existsByEmail(new Email(sellerUser.getEmail().value().toUpperCase())));
        assertThrows(IllegalStateException.class, () -> ctx.adminUseCase.registerUser(admin,
                new User(new FullName("Otra", "Persona"), sellerUser.getEmail(), new IdentityDocument("777"),
                        UserRole.BUYER)));

        ctx.adminUseCase.onboardSeller(admin, sellerUser,
                Warehouse.ofMarketplace("Centro", WarehouseType.PHYSICAL, address("Bodega 2")));
        assertTrue(ctx.warehouses.existsByName("CENTRO"));
        ctx.sellerUseCase.registerProduct(sellerUser, new Product(UUID.randomUUID(), "Taza", null,
                ProductType.PHYSICAL, new Money(new BigDecimal("15000"))));
        assertThrows(IllegalStateException.class, () -> ctx.sellerUseCase.registerProduct(sellerUser,
                new Product(UUID.randomUUID(), "taza", null, ProductType.PHYSICAL, new Money(new BigDecimal("1")))));
    }

    @Test
    void aBlockedUserIsReadBackBlocked() {
        JpaTestContext ctx = new JpaTestContext();
        User admin = newUser(UserRole.ADMIN);
        ctx.users.save(admin);
        User supervisor = ctx.adminUseCase.registerUser(admin, newUser(UserRole.SUPERVISOR));

        ctx.adminUseCase.blockUser(admin, supervisor);

        assertEquals(UserStatus.BLOCKED, ctx.users.findById(supervisor.getId()).orElseThrow().getStatus());
        assertEquals(2, ctx.supervisorUseCase.consultUsers(admin).size());
        assertEquals(AuditOperation.USER_BLOCKING, ctx.adminUseCase.consultAuditLog(admin).get(0).getOperation());
    }
}