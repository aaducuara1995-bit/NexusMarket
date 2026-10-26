package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmOrderServiceTest {

    private final ConfirmOrderService service = new ConfirmOrderService(
            new BuyerValidationService(),
            new ReserveInventoryService(),
            new ReleaseReservationService());

    private final User user = Fixtures.user(UserRole.BUYER);
    private final Buyer buyer = Fixtures.buyer(user);

    private Product publishedProduct(ProductType type) {
        Product product = Fixtures.product(type);
        product.publish();
        return product;
    }

    @Test
    void confirmsTheOrderAndReservesStock() {
        Product product = publishedProduct(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        Cart cart = new Cart(buyer);
        cart.addProduct(product, 3);

        var result = service.confirm(user, buyer, cart, List.of(inventory));

        assertEquals(OrderStatus.PENDING_PAYMENT, result.order().getStatus());
        assertEquals(1, result.reservations().size());
        assertEquals(1, result.movements().size());
        assertEquals(3, inventory.getReserved());
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void reservesEachProductInTheCart() {
        Product a = publishedProduct(ProductType.PHYSICAL);
        Product b = publishedProduct(ProductType.PHYSICAL);
        Inventory inventoryA = Fixtures.inventory(a, 10);
        Inventory inventoryB = Fixtures.inventory(b, 10);
        Cart cart = new Cart(buyer);
        cart.addProduct(a, 2);
        cart.addProduct(b, 4);

        var result = service.confirm(user, buyer, cart, List.of(inventoryA, inventoryB));

        assertEquals(2, result.reservations().size());
        assertEquals(2, inventoryA.getReserved());
        assertEquals(4, inventoryB.getReserved());
    }

    @Test
    void ifStockIsMissingLeavesNoReservationsAndKeepsTheCart() {
        Product a = publishedProduct(ProductType.PHYSICAL);
        Product b = publishedProduct(ProductType.PHYSICAL);
        Inventory inventoryA = Fixtures.inventory(a, 10);
        Inventory inventoryB = Fixtures.inventory(b, 1);
        Cart cart = new Cart(buyer);
        cart.addProduct(a, 3);
        cart.addProduct(b, 5);

        assertThrows(IllegalStateException.class,
                () -> service.confirm(user, buyer, cart, List.of(inventoryA, inventoryB)));

        assertEquals(0, inventoryA.getReserved());
        assertEquals(0, inventoryB.getReserved());
        assertEquals(2, cart.getItems().size());
    }

    @Test
    void rejectsAnEmptyCart() {
        Cart cart = new Cart(buyer);

        assertThrows(IllegalArgumentException.class,
                () -> service.confirm(user, buyer, cart, List.of()));
    }

    @Test
    void rejectsAProductNoLongerPublished() {
        Product product = publishedProduct(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        Cart cart = new Cart(buyer);
        cart.addProduct(product, 2);
        product.suspend();

        assertThrows(IllegalStateException.class,
                () -> service.confirm(user, buyer, cart, List.of(inventory)));
        assertEquals(0, inventory.getReserved());
    }

    @Test
    void restrictedBuyerCannotConfirm() {
        Product product = publishedProduct(ProductType.DIGITAL);
        Cart cart = new Cart(buyer);
        cart.addProduct(product, 1);
        buyer.restrict();

        assertThrows(IllegalStateException.class,
                () -> service.confirm(user, buyer, cart, List.of()));
    }

    @Test
    void digitalProductsDoNotReserveInventory() {
        Product digital = publishedProduct(ProductType.DIGITAL);
        Cart cart = new Cart(buyer);
        cart.addProduct(digital, 2);

        var result = service.confirm(user, buyer, cart, List.of());

        assertTrue(result.reservations().isEmpty());
        assertTrue(result.order().isDigitalOnly());
        assertTrue(cart.getItems().isEmpty());
    }
}