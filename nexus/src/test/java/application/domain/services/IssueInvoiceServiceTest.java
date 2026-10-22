package application.domain.services;

import application.domain.entities.Cart;
import application.domain.entities.Invoice;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IssueInvoiceServiceTest {

    private final IssueInvoiceService service = new IssueInvoiceService();

    private Order order(boolean paid) {
        Product product = Fixtures.product(ProductType.DIGITAL);
        product.publish();
        Cart cart = new Cart(Fixtures.buyer(Fixtures.user(UserRole.BUYER)));
        cart.addProduct(product, 2);
        Order order = new Order(cart);
        if (paid)
            order.pay();
        return order;
    }

    @Test
    void issuesTheInvoiceOfAPaidOrderWithTheOrderTotal() {
        Order order = order(true);

        Invoice invoice = service.issue(order, List.of());

        assertEquals(order.getId(), invoice.getOrder().getId());
        assertEquals(0, invoice.getTotal().amount().compareTo(order.calculateTotal().amount()));
    }

    @Test
    void rejectsInvoicingAnUnpaidOrder() {
        Order order = order(false);

        assertThrows(IllegalStateException.class, () -> service.issue(order, List.of()));
    }

    @Test
    void rejectsInvoicingTheSameOrderTwice() {
        Order order = order(true);
        Invoice existing = service.issue(order, List.of());

        assertThrows(IllegalStateException.class,
                () -> service.issue(order, List.of(existing)));
    }

    @Test
    void anotherOrdersInvoiceDoesNotPreventInvoicing() {
        Invoice fromAnotherOrder = service.issue(order(true), List.of());
        Order order = order(true);

        Invoice invoice = service.issue(order, List.of(fromAnotherOrder));

        assertEquals(order.getId(), invoice.getOrder().getId());
    }
}