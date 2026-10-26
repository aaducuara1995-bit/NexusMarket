package application.domain.services;

import application.domain.entities.Cart;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeliverDigitalOrderServiceTest {

    private final DeliverDigitalOrderService service = new DeliverDigitalOrderService();

    private Order orderOf(ProductType type) {
        Product product = Fixtures.product(type);
        product.publish();
        Cart cart = new Cart(Fixtures.buyer(Fixtures.user(UserRole.BUYER)));
        cart.addProduct(product, 1);
        return new Order(cart);
    }

    @Test
    void deliversAPaidDigitalOnlyOrder() {
        Order order = orderOf(ProductType.DIGITAL);
        order.pay();

        service.deliver(order);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void rejectsOrdersWithPhysicalProducts() {
        Order order = orderOf(ProductType.PHYSICAL);
        order.pay();

        assertThrows(IllegalStateException.class, () -> service.deliver(order));
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void rejectsUnpaidOrders() {
        Order order = orderOf(ProductType.DIGITAL);

        assertThrows(IllegalStateException.class, () -> service.deliver(order));
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
    }

    @Test
    void isNotDeliveredTwice() {
        Order order = orderOf(ProductType.DIGITAL);
        order.pay();
        service.deliver(order);

        assertThrows(IllegalStateException.class, () -> service.deliver(order));
    }
}