package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfirmDeliveryServiceTest {

    private record Scenario(Order order, Shipment shipment, List<InventoryReservation> reservations) {
    }

    private final ConfirmOrderService confirmation = new ConfirmOrderService(
            new BuyerValidationService(), new ReserveInventoryService(), new ReleaseReservationService());
    private final ShipOrderService shipping =
            new ShipOrderService(new AuthorizationService(), new InventoryOutflowService());
    private final ConfirmDeliveryService service =
            new ConfirmDeliveryService(new AuthorizationService());

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

        return new Scenario(result.order(), shipment, result.reservations());
    }

    private Scenario shippedOrder() {
        Scenario e = paidOrderWithShipment();
        shipping.ship(operator, e.order(), e.shipment(), e.reservations());
        return e;
    }

    @Test
    void confirmsDeliveryAndCompletesTheOrder() {
        Scenario e = shippedOrder();

        service.confirm(operator, e.order(), e.shipment());

        assertEquals(ShipmentStatus.DELIVERED, e.shipment().getStatus());
        assertEquals(OrderStatus.DELIVERED, e.order().getStatus());
    }

    @Test
    void onlyLogisticOperatorConfirmsDelivery() {
        Scenario e = shippedOrder();
        User seller = Fixtures.user(UserRole.SELLER);

        assertThrows(AccessDeniedException.class,
                () -> service.confirm(seller, e.order(), e.shipment()));
        assertEquals(OrderStatus.SHIPPED, e.order().getStatus());
    }

    @Test
    void rejectsAShipmentNotYetShipped() {
        Scenario e = paidOrderWithShipment();

        assertThrows(IllegalStateException.class,
                () -> service.confirm(operator, e.order(), e.shipment()));
        assertEquals(OrderStatus.PAID, e.order().getStatus());
    }

    @Test
    void rejectsAShipmentOfAnotherOrder() {
        Scenario a = shippedOrder();
        Scenario b = shippedOrder();

        assertThrows(IllegalArgumentException.class,
                () -> service.confirm(operator, a.order(), b.shipment()));
    }

    @Test
    void isNotConfirmedTwice() {
        Scenario e = shippedOrder();
        service.confirm(operator, e.order(), e.shipment());

        assertThrows(IllegalStateException.class,
                () -> service.confirm(operator, e.order(), e.shipment()));
    }
}