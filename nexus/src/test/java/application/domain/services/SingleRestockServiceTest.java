package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SingleRestockServiceTest {

    private final RestockInventoryService service = new RestockInventoryService(
            new InventoryAccessService(new AuthorizationService(), new SellerValidationService()));

    private final User user = Fixtures.user(UserRole.SELLER);
    private final Seller seller = Fixtures.seller(user);

    private ReturnRequest approvedReturn(Product product, int purchasedQuantity) {
        ReturnRequest returnRequest = new ReturnRequest(
                Fixtures.deliveredOrder(product, purchasedQuantity), "Wrong size");
        returnRequest.approve();
        return returnRequest;
    }

    @Test
    void canRestockInSeveralPartsUpToThePurchasedTotal() {
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory warehouseA = Fixtures.inventory(product, 10);
        Inventory warehouseB = Fixtures.inventory(product, 10);
        ReturnRequest returnRequest = approvedReturn(product, 3);

        service.restock(user, seller, warehouseA, 2, returnRequest);
        service.restock(user, seller, warehouseB, 1, returnRequest);

        assertEquals(12, warehouseA.getQuantity());
        assertEquals(11, warehouseB.getQuantity());
        assertEquals(0, returnRequest.unitsPendingRestock(product.getId()));
    }

    @Test
    void sameReturnIsNotRestockedTwice() {
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        ReturnRequest returnRequest = approvedReturn(product, 3);
        service.restock(user, seller, inventory, 3, returnRequest);

        assertThrows(IllegalArgumentException.class,
                () -> service.restock(user, seller, inventory, 1, returnRequest));
        assertEquals(13, inventory.getQuantity());
    }
}