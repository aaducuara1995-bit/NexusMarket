package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.MovementType;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterInitialInventoryServiceTest {

    private final RegisterInitialInventoryService service = new RegisterInitialInventoryService(
            new InventoryAccessService(new AuthorizationService(), new SellerValidationService()));

    @Test
    void sellerRegistersInitialStockInTheirWarehouse() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Warehouse warehouse = Warehouse.ofSeller(seller.getId(), "My warehouse",
                WarehouseType.PHYSICAL, Fixtures.address());

        var result = service.register(user, seller, product, warehouse, 10, List.of());

        assertEquals(10, result.inventory().getQuantity());
        assertEquals(1, result.movements().size());
        assertEquals(MovementType.INFLOW, result.movements().get(0).getType());
        assertEquals(10, result.movements().get(0).getQuantity());
    }

    @Test
    void sellerCanAlsoUseAMarketplaceWarehouse() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);

        var result = service.register(user, seller, product, Fixtures.warehouse(), 5, List.of());

        assertEquals(5, result.inventory().getQuantity());
    }

    @Test
    void sellerCannotUseAnotherSellersWarehouse() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Warehouse foreign = Warehouse.ofSeller(UUID.randomUUID(), "Foreign warehouse",
                WarehouseType.PHYSICAL, Fixtures.address());

        assertThrows(AccessDeniedException.class,
                () -> service.register(user, seller, product, foreign, 10, List.of()));
    }

    @Test
    void productCannotBeDuplicatedInTheSameWarehouse() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Warehouse warehouse = Fixtures.warehouse();
        Inventory existing = new Inventory(product, warehouse, 3);

        assertThrows(IllegalStateException.class,
                () -> service.register(user, seller, product, warehouse, 10, List.of(existing)));
    }

    @Test
    void onlyPhysicalProductsHaveInventory() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product digital = Fixtures.product(seller.getId(), ProductType.DIGITAL);

        assertThrows(IllegalStateException.class,
                () -> service.register(user, seller, digital, Fixtures.warehouse(), 10, List.of()));
    }

    @Test
    void logisticOperatorCanRegisterInAnyWarehouse() {
        User operator = Fixtures.user(UserRole.LOGISTIC_OPERATOR);
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Warehouse ofSeller = Warehouse.ofSeller(UUID.randomUUID(), "Seller warehouse",
                WarehouseType.PHYSICAL, Fixtures.address());

        var result = service.register(operator, null, product, ofSeller, 8, List.of());

        assertEquals(8, result.inventory().getQuantity());
    }

    @Test
    void zeroInitialStockCreatesNoMovement() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);

        var result = service.register(user, seller, product, Fixtures.warehouse(), 0, List.of());

        assertTrue(result.movements().isEmpty());
    }
}