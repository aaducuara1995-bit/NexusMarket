package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.CartItem;
import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Invoice;
import application.domain.entities.Order;
import application.domain.entities.OrderItem;
import application.domain.entities.Product;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.CommercialStatus;
import application.domain.enums.InventoryStatus;
import application.domain.enums.MovementType;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReservationStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.enums.WarehouseOwnership;
import application.domain.enums.WarehouseType;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The persistence adapters rebuild entities with restore(...): id and state must survive intact. */
class EntityRestoreTest {

    private static final LocalDateTime YESTERDAY = LocalDateTime.now().minusDays(1);

    @Test
    void aRestoredUserKeepsItsIdAndStatus() {
        User original = Fixtures.user(UserRole.SELLER);
        original.block();

        User restored = User.restore(original.getId(), original.getFullName(), original.getEmail(),
                original.getDocument(), original.getRole(), original.getStatus());

        assertEquals(original.getId(), restored.getId());
        assertEquals(UserStatus.BLOCKED, restored.getStatus());
        assertDoesNotThrow(restored::activate);
    }

    @Test
    void aRestoredBuyerKeepsAllItsAddresses() {
        Address office = new Address("Oficina 2", "Bello", "Antioquia", "051050");
        Buyer restored = Buyer.restore(UUID.randomUUID(), UUID.randomUUID(), Fixtures.address(),
                List.of(office), CommercialStatus.RESTRICTED);

        assertTrue(restored.hasAddress(office));
        assertFalse(restored.canPurchase());
    }

    @Test
    void aRestoredProductKeepsStatusAndVariants() {
        UUID id = UUID.randomUUID();
        Product restored = Product.restore(id, UUID.randomUUID(), "Shirt", "Cotton", ProductType.PHYSICAL,
                new Money(new BigDecimal("30000")), ProductStatus.PUBLISHED, List.of("S", "M"));

        assertEquals(id, restored.getId());
        assertTrue(restored.isPublished());
        assertEquals(List.of("S", "M"), restored.getVariants());
        assertThrows(IllegalStateException.class, () -> restored.addVariant("M"));
    }

    @Test
    void aRestoredWarehouseKeepsItsOwner() {
        UUID sellerId = UUID.randomUUID();
        Warehouse restored = Warehouse.restore(UUID.randomUUID(), "Norte", WarehouseType.PHYSICAL,
                Fixtures.address(), WarehouseOwnership.SELLER, sellerId);

        assertTrue(restored.belongsTo(sellerId));
        assertThrows(IllegalArgumentException.class, () -> Warehouse.restore(UUID.randomUUID(), "Sur",
                WarehouseType.PHYSICAL, Fixtures.address(), WarehouseOwnership.SELLER, null));
    }

    @Test
    void aRestoredInventoryKeepsItsReservedUnits() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory restored = Inventory.restore(UUID.randomUUID(), product, Fixtures.warehouse(),
                10, 4, InventoryStatus.AVAILABLE);

        assertEquals(6, restored.getAvailable());
        assertThrows(IllegalStateException.class, () -> restored.adjust(-7));
        assertThrows(IllegalArgumentException.class, () -> Inventory.restore(UUID.randomUUID(), product,
                Fixtures.warehouse(), 3, 5, InventoryStatus.AVAILABLE));
    }

    @Test
    void restoringAReservationDoesNotReserveAgain() {
        Inventory inventory = Inventory.restore(UUID.randomUUID(), Fixtures.product(ProductType.PHYSICAL),
                Fixtures.warehouse(), 10, 3, InventoryStatus.AVAILABLE);

        InventoryReservation restored = InventoryReservation.restore(UUID.randomUUID(), UUID.randomUUID(),
                inventory, 3, YESTERDAY, ReservationStatus.ACTIVE);

        assertEquals(3, inventory.getReserved());
        restored.confirmOutflow();
        assertEquals(7, inventory.getQuantity());
        assertEquals(0, inventory.getReserved());
    }

    @Test
    void aRestoredMovementKeepsItsDate() {
        InventoryMovement restored = InventoryMovement.restore(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), MovementType.SALE, -2, "Sale", YESTERDAY);

        assertEquals(YESTERDAY, restored.getDate());
        assertEquals(-2, restored.getQuantity());
    }

    @Test
    void aRestoredCartKeepsItsLines() {
        Product product = Fixtures.product(ProductType.DIGITAL);
        product.publish();
        Cart restored = Cart.restore(UUID.randomUUID(), Fixtures.buyer(Fixtures.user(UserRole.BUYER)),
                List.of(CartItem.restore(product, 2)));

        restored.addProduct(product, 1);

        assertEquals(1, restored.getItems().size());
        assertEquals(3, restored.getItems().get(0).getQuantity());
    }

    @Test
    void aRestoredOrderContinuesItsLifecycle() {
        Buyer buyer = Fixtures.buyer(Fixtures.user(UserRole.BUYER));
        OrderItem line = OrderItem.restore(UUID.randomUUID(), "Shirt", ProductType.PHYSICAL, 2,
                new Money(new BigDecimal("30000")));

        Order restored = Order.restore(UUID.randomUUID(), buyer, List.of(line), YESTERDAY,
                buyer.getMainAddress(), OrderStatus.PAID);

        assertEquals(new Money(new BigDecimal("60000")), restored.calculateTotal());
        restored.ship();
        assertEquals(OrderStatus.SHIPPED, restored.getStatus());
        assertThrows(IllegalArgumentException.class, () -> Order.restore(UUID.randomUUID(), buyer,
                List.of(), YESTERDAY, buyer.getMainAddress(), OrderStatus.PAID));
    }

    @Test
    void invoiceShipmentAndRefundAreRestoredWithTheirState() {
        Order order = Fixtures.deliveredOrder(Fixtures.product(ProductType.PHYSICAL), 1);

        Invoice invoice = Invoice.restore(UUID.randomUUID(), order, order.calculateTotal(), YESTERDAY);
        Shipment shipment = Shipment.restore(UUID.randomUUID(), order, order.getDeliveryAddress(),
                ShipmentStatus.DELIVERED);
        Refund refund = Refund.restore(UUID.randomUUID(), order, order.calculateTotal(), RefundStatus.PENDING);

        assertEquals(YESTERDAY, invoice.getDate());
        assertThrows(IllegalStateException.class, shipment::deliver);
        refund.process();
        assertEquals(RefundStatus.PROCESSED, refund.getStatus());
    }

    @Test
    void aRestoredReturnRemembersTheUnitsAlreadyRestocked() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Order order = Fixtures.deliveredOrder(product, 3);

        ReturnRequest restored = ReturnRequest.restore(UUID.randomUUID(), order, "Broken",
                ReturnStatus.APPROVED, Map.of(product.getId(), 2));

        assertEquals(1, restored.unitsPendingRestock(product.getId()));
        assertEquals(Map.of(product.getId(), 2), restored.getRestockedUnits());
        assertThrows(IllegalArgumentException.class, () -> restored.registerRestock(product.getId(), 2));
    }

    @Test
    void nothingIsRestoredWithoutAnId() {
        assertThrows(IllegalArgumentException.class, () -> Seller.restore(null, UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> User.restore(null, null, null, null, null, null));
    }
}