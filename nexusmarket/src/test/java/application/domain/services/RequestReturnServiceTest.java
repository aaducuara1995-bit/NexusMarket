package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.ReturnStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestReturnServiceTest {

    private final RequestReturnService service =
            new RequestReturnService(new AuthorizationService());

    private final User user = Fixtures.user(UserRole.BUYER);
    private final Buyer buyer = Fixtures.buyer(user);

    private Order orderOf(Buyer owner, boolean delivered) {
        Product product = Fixtures.product(ProductType.DIGITAL);
        product.publish();
        Cart cart = new Cart(owner);
        cart.addProduct(product, 1);
        Order order = new Order(cart);
        order.pay();
        if (delivered) {
            order.ship();
            order.complete();
        }
        return order;
    }

    @Test
    void buyerRequestsTheReturnOfADeliveredOrder() {
        Order order = orderOf(buyer, true);

        ReturnRequest returnRequest = service.request(user, buyer, order, "Arrived damaged", List.of());

        assertEquals(ReturnStatus.REQUESTED, returnRequest.getStatus());
        assertEquals(order.getId(), returnRequest.getOrder().getId());
    }

    @Test
    void rejectsAnOrderNotYetDelivered() {
        Order order = orderOf(buyer, false);

        assertThrows(IllegalStateException.class,
                () -> service.request(user, buyer, order, "No longer wanted", List.of()));
    }

    @Test
    void rejectsAnotherBuyersOrder() {
        Order foreign = orderOf(Fixtures.buyer(Fixtures.user(UserRole.BUYER)), true);

        assertThrows(AccessDeniedException.class,
                () -> service.request(user, buyer, foreign, "Arrived damaged", List.of()));
    }

    @Test
    void rejectsIfTheOrderAlreadyHasAReturnInProgress() {
        Order order = orderOf(buyer, true);
        ReturnRequest previous = service.request(user, buyer, order, "Arrived damaged", List.of());

        assertThrows(IllegalStateException.class,
                () -> service.request(user, buyer, order, "Another reason", List.of(previous)));
    }

    @Test
    void allowsRequestingAgainIfThePreviousWasRejected() {
        Order order = orderOf(buyer, true);
        ReturnRequest previous = service.request(user, buyer, order, "Arrived damaged", List.of());
        previous.reject();

        ReturnRequest newAddress = service.request(user, buyer, order, "Still damaged", List.of(previous));

        assertEquals(ReturnStatus.REQUESTED, newAddress.getStatus());
    }

    @Test
    void sellerCannotRequestReturns() {
        User seller = Fixtures.user(UserRole.SELLER);
        Buyer profile = Fixtures.buyer(seller);
        Order order = orderOf(profile, true);

        assertThrows(AccessDeniedException.class,
                () -> service.request(seller, profile, order, "Arrived damaged", List.of()));
    }

    @Test
    void adminCannotRequestOnBehalfOfTheBuyer() {
        User admin = Fixtures.user(UserRole.ADMIN);
        Order order = orderOf(buyer, true);

        assertThrows(AccessDeniedException.class,
                () -> service.request(admin, buyer, order, "Arrived damaged", List.of()));
    }
}