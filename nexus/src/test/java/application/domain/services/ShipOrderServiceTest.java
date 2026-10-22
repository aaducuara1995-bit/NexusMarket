package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.enums.MovementType;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipOrderServiceTest {

    private record Scenario(Order order, Shipment shipment,
                             List<InventoryReservation> reservations, Inventory inventory) {
    }

    private final ConfirmOrderService confirmation = new ConfirmOrderService(
            new BuyerValidationService(), new ReserveInventoryService(), new ReleaseReservationService());
    private final ShipOrderService service =
            new ShipOrderService(new AuthorizationService(), new InventoryOutflowService());

    private final User operator = Fixtures.user(UserRole.LOGISTIC_OPERATOR);
    private final User buyerUser = Fixtures.user(UserRole.BUYER);
    private final Buyer buyer = Fixtures.buyer(buyerUser);

    private Scenario paidOrderWithShipment() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        product.publish();
        Inventory inventory = Fixtures.inventory(product, 10);
        Cart cart = new Cart(buyer);
        cart.addProduct(product, 3);

        var result = confirmation.confirm(buyerUser, buyer, cart, List.of(inventory));
        result.order().pay();
        Shipment shipment = new Shipment(result.order(), buyer.getMainAddress());

        return new Scenario(result.order(), shipment, result.reservations(), inventory);
    }

    @Test
    void shipsTheOrderAndDeductsStock() {
        var e = paidOrderWithShipment();

        var movements = service.ship(operator, e.order(), e.shipment(), e.reservations());

        assertEquals(OrderStatus.SHIPPED, e.order().getStatus());
        assertEquals(ShipmentStatus.SHIPPED, e.shipment().getStatus());
        assertEquals(7, e.inventory().getQuantity());
        assertEquals(0, e.inventory().getReserved());
        assertEquals(MovementType.SALE, movements.get(0).getType());
    }

    @Test
    void onlyLogisticOperatorShips() {
        var e = paidOrderWithShipment();
        User seller = Fixtures.user(UserRole.SELLER);

        assertThrows(AccessDeniedException.class,
                () -> service.ship(seller, e.order(), e.shipment(), e.reservations()));
        assertEquals(OrderStatus.PAID, e.order().getStatus());
    }

    @Test
    void rejectsAShipmentOfAnotherOrder() {
        var a = paidOrderWithShipment();
        var b = paidOrderWithShipment();

        assertThrows(IllegalArgumentException.class,
                () -> service.ship(operator, a.order(), b.shipment(), a.reservations()));
    }

    @Test
    void isNotShippedTwice() {
        var e = paidOrderWithShipment();
        service.ship(operator, e.order(), e.shipment(), e.reservations());

        assertThrows(IllegalStateException.class,
                () -> service.ship(operator, e.order(), e.shipment(), e.reservations()));
    }

    @Test
    void rejectsShippingWithoutReservedStock() {
        var e = paidOrderWithShipment();

        assertThrows(IllegalStateException.class,
                () -> service.ship(operator, e.order(), e.shipment(), List.of()));
        assertEquals(OrderStatus.PAID, e.order().getStatus());
        assertEquals(ShipmentStatus.PENDING, e.shipment().getStatus());
    }

    @Test
    void rejectsReservationsOfAnotherOrderWithoutChangingAnything() {
        var a = paidOrderWithShipment();
        var b = paidOrderWithShipment();

        assertThrows(IllegalArgumentException.class,
                () -> service.ship(operator, a.order(), a.shipment(), b.reservations()));

        assertEquals(OrderStatus.PAID, a.order().getStatus());
        assertEquals(ShipmentStatus.PENDING, a.shipment().getStatus());
        assertEquals(3, b.inventory().getReserved());
    }
}