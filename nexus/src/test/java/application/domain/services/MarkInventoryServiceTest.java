package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.InventoryStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarkInventoryServiceTest {

    private final MarkInventoryService service = new MarkInventoryService(
            new InventoryAccessService(new AuthorizationService(), new SellerValidationService()));

    @Test
    void sellerMarksTheirInventoryAsDamaged() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);

        service.markDamaged(user, seller, inventory);

        assertEquals(InventoryStatus.DAMAGED, inventory.getStatus());
    }

    @Test
    void inventoryWithActiveReservationsIsNotMarkedDamaged() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        inventory.reserve(2);

        assertThrows(IllegalStateException.class,
                () -> service.markDamaged(user, seller, inventory));
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    void canBeBlockedAndEnabledAgain() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);

        service.block(user, seller, inventory);
        assertEquals(InventoryStatus.BLOCKED, inventory.getStatus());

        service.enable(user, seller, inventory);
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    void buyerCannotMarkInventory() {
        User buyer = Fixtures.user(UserRole.BUYER);
        Inventory inventory = Fixtures.inventory(Fixtures.product(ProductType.PHYSICAL), 10);

        assertThrows(AccessDeniedException.class,
                () -> service.markDamaged(buyer, null, inventory));
    }

    @Test
    void logisticOperatorCanMarkInventory() {
        User operator = Fixtures.user(UserRole.LOGISTIC_OPERATOR);
        Inventory inventory = Fixtures.inventory(Fixtures.product(ProductType.PHYSICAL), 10);

        service.markDamaged(operator, null, inventory);

        assertEquals(InventoryStatus.DAMAGED, inventory.getStatus());
    }
}