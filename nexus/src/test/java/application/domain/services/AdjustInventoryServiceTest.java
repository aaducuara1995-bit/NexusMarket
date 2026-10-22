package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.MovementType;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdjustInventoryServiceTest {

    private final AdjustInventoryService service = new AdjustInventoryService(
            new InventoryAccessService(new AuthorizationService(), new SellerValidationService()));

    @Test
    void positiveAdjustmentAddsAndRecordsTheMovement() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);

        var movement = service.adjust(user, seller, inventory, 5, "Physical count");

        assertEquals(15, inventory.getQuantity());
        assertEquals(MovementType.ADJUSTMENT, movement.getType());
        assertEquals(5, movement.getQuantity());
    }

    @Test
    void negativeAdjustmentSubtractsAndRecordsTheMovement() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);

        var movement = service.adjust(user, seller, inventory, -3, "Shrinkage");

        assertEquals(7, inventory.getQuantity());
        assertEquals(-3, movement.getQuantity());
    }

    @Test
    void doesNotAllowNegativeStock() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);

        assertThrows(IllegalStateException.class,
                () -> service.adjust(user, seller, inventory, -11, "Shrinkage"));
        assertEquals(10, inventory.getQuantity());
    }

    @Test
    void requiresAReasonAndChangesNothingWithoutIt() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.adjust(user, seller, inventory, 5, "  "));
        assertEquals(10, inventory.getQuantity());
    }

    @Test
    void sellerCannotAdjustAnotherSellersInventory() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Inventory foreign = Fixtures.inventory(Fixtures.product(ProductType.PHYSICAL), 10);

        assertThrows(AccessDeniedException.class,
                () -> service.adjust(user, seller, foreign, 5, "Physical count"));
        assertEquals(10, foreign.getQuantity());
    }
}