package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderDeliveryAddressTest {

    private final User user = Fixtures.user(UserRole.BUYER);
    private final Buyer buyer = Fixtures.buyer(user);
    private final Address other = new Address("Carrera 5 # 6-7", "Bello", "Antioquia", "051050");

    private Cart cartWithProduct() {
        Product product = Fixtures.product(ProductType.DIGITAL);
        product.publish();
        Cart cart = new Cart(buyer);
        cart.addProduct(product, 1);
        return cart;
    }

    @Test
    void withoutAChosenAddressTheMainAddressIsUsed() {
        Order order = new Order(cartWithProduct());

        assertEquals(buyer.getMainAddress(), order.getDeliveryAddress());
    }

    @Test
    void acceptsAnAdditionalAddressOfTheBuyer() {
        buyer.addAdditionalAddress(other);

        Order order = new Order(cartWithProduct(), other);

        assertEquals(other, order.getDeliveryAddress());
    }

    @Test
    void rejectsAnAddressThatDoesNotBelongToTheBuyer() {
        assertThrows(IllegalArgumentException.class, () -> new Order(cartWithProduct(), other));
    }

    @Test
    void confirmOrderKeepsTheChosenAddressAndLeavesTheCartIntactIfItIsInvalid() {
        ConfirmOrderService service = new ConfirmOrderService(
                new BuyerValidationService(), new ReserveInventoryService(), new ReleaseReservationService());
        Cart cart = cartWithProduct();

        assertThrows(IllegalArgumentException.class,
                () -> service.confirm(user, buyer, cart, List.of(), other));
        assertEquals(1, cart.getItems().size());

        buyer.addAdditionalAddress(other);
        Order order = service.confirm(user, buyer, cart, List.of(), other).order();

        assertEquals(other, order.getDeliveryAddress());
        assertTrue(cart.getItems().isEmpty());
    }
}