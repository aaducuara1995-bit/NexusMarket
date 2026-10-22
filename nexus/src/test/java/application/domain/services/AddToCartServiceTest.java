package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddToCartServiceTest {

    private final AddToCartService service =
            new AddToCartService(new BuyerValidationService());

    private final User user = Fixtures.user(UserRole.BUYER);
    private final Buyer buyer = Fixtures.buyer(user);

    private Product publishedProduct(ProductType type) {
        Product product = Fixtures.product(type);
        product.publish();
        return product;
    }

    @Test
    void addsAPublishedProductWithStock() {
        Product product = publishedProduct(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        Cart cart = new Cart(buyer);

        service.add(user, buyer, cart, product, 3, List.of(inventory));

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addsQuantityIfTheProductIsAlreadyInTheCart() {
        Product product = publishedProduct(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        Cart cart = new Cart(buyer);

        service.add(user, buyer, cart, product, 3, List.of(inventory));
        service.add(user, buyer, cart, product, 4, List.of(inventory));

        assertEquals(1, cart.getItems().size());
        assertEquals(7, cart.getItems().get(0).getQuantity());
    }

    @Test
    void rejectsIfTotalQuantityExceedsStock() {
        Product product = publishedProduct(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 5);
        Cart cart = new Cart(buyer);
        service.add(user, buyer, cart, product, 3, List.of(inventory));

        assertThrows(IllegalStateException.class,
                () -> service.add(user, buyer, cart, product, 3, List.of(inventory)));
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void rejectsAnUnpublishedProduct() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        Cart cart = new Cart(buyer);

        assertThrows(IllegalStateException.class,
                () -> service.add(user, buyer, cart, product, 1, List.of(inventory)));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void digitalProductDoesNotRequireInventory() {
        Product digital = publishedProduct(ProductType.DIGITAL);
        Cart cart = new Cart(buyer);

        service.add(user, buyer, cart, digital, 2, List.of());

        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void restrictedBuyerCannotAdd() {
        Product digital = publishedProduct(ProductType.DIGITAL);
        Cart cart = new Cart(buyer);
        buyer.restrict();

        assertThrows(IllegalStateException.class,
                () -> service.add(user, buyer, cart, digital, 1, List.of()));
    }

    @Test
    void anotherBuyersCartCannotBeUsed() {
        Product digital = publishedProduct(ProductType.DIGITAL);
        Cart fromAnother = new Cart(Fixtures.buyer(Fixtures.user(UserRole.BUYER)));

        assertThrows(AccessDeniedException.class,
                () -> service.add(user, buyer, fromAnother, digital, 1, List.of()));
    }
}