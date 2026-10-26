package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayOrderServiceTest {

    private final PayOrderService service =
            new PayOrderService(new BuyerValidationService());

    private final User user = Fixtures.user(UserRole.BUYER);
    private final Buyer buyer = Fixtures.buyer(user);

    // Two units of a 45,000 COP product: the total is 90,000 COP.
    private Order orderOfNinetyThousand() {
        Product product = Fixtures.product(ProductType.DIGITAL);
        product.publish();
        Cart cart = new Cart(buyer);
        cart.addProduct(product, 2);
        return new Order(cart);
    }

    @Test
    void buyerPaysTheExactTotal() {
        Order order = orderOfNinetyThousand();

        service.pay(user, buyer, order, new Money(new BigDecimal("90000")));

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void rejectsAnAmountDifferentFromTheTotal() {
        Order order = orderOfNinetyThousand();

        assertThrows(IllegalArgumentException.class,
                () -> service.pay(user, buyer, order, new Money(new BigDecimal("80000"))));
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
    }

    @Test
    void rejectsAPaymentInAnotherCurrency() {
        Order order = orderOfNinetyThousand();

        assertThrows(IllegalArgumentException.class,
                () -> service.pay(user, buyer, order, new Money(new BigDecimal("90000"), "USD")));
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
    }

    @Test
    void orderMustBelongToThePayingBuyer() {
        Order order = orderOfNinetyThousand();
        User otherUser = Fixtures.user(UserRole.BUYER);
        Buyer other = Fixtures.buyer(otherUser);

        assertThrows(AccessDeniedException.class,
                () -> service.pay(otherUser, other, order, new Money(new BigDecimal("90000"))));
    }

    @Test
    void orderIsNotPaidTwice() {
        Order order = orderOfNinetyThousand();
        service.pay(user, buyer, order, new Money(new BigDecimal("90000")));

        assertThrows(IllegalStateException.class,
                () -> service.pay(user, buyer, order, new Money(new BigDecimal("90000"))));
    }
}