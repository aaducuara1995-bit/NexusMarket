package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.MovementType;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestockInventoryServiceTest {

    private final RestockInventoryService service = new RestockInventoryService(
            new InventoryAccessService(new AuthorizationService(), new SellerValidationService()));

    private ReturnRequest approvedReturn(Product product, int purchasedQuantity) {
        Order order = Fixtures.deliveredOrder(product, purchasedQuantity);
        ReturnRequest returnRequest = new ReturnRequest(order, "Wrong size");
        returnRequest.approve();
        return returnRequest;
    }

    @Test
    void restocksTheUnitsOfAnApprovedReturn() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        ReturnRequest returnRequest = approvedReturn(product, 3);

        var movement = service.restock(user, seller, inventory, 2, returnRequest);

        assertEquals(12, inventory.getQuantity());
        assertEquals(MovementType.RETURN, movement.getType());
        assertEquals(2, movement.getQuantity());
    }

    @Test
    void rejectsAReturnThatIsNotApproved() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        Order order = Fixtures.deliveredOrder(product, 3);
        ReturnRequest requested = new ReturnRequest(order, "Wrong size");

        assertThrows(IllegalStateException.class,
                () -> service.restock(user, seller, inventory, 2, requested));
        assertEquals(10, inventory.getQuantity());
    }

    @Test
    void rejectsAProductThatWasNotInTheOrder() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product purchased = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Product other = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory otherInventory = Fixtures.inventory(other, 10);
        ReturnRequest returnRequest = approvedReturn(purchased, 3);

        assertThrows(IllegalArgumentException.class,
                () -> service.restock(user, seller, otherInventory, 1, returnRequest));
    }

    @Test
    void rejectsRestockingMoreUnitsThanPurchased() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        ReturnRequest returnRequest = approvedReturn(product, 3);

        assertThrows(IllegalArgumentException.class,
                () -> service.restock(user, seller, inventory, 4, returnRequest));
        assertEquals(10, inventory.getQuantity());
    }

    @Test
    void onlyRestocksAnAvailableInventory() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        inventory.block();
        ReturnRequest returnRequest = approvedReturn(product, 3);

        assertThrows(IllegalStateException.class,
                () -> service.restock(user, seller, inventory, 2, returnRequest));
        assertEquals(10, inventory.getQuantity());
    }
}