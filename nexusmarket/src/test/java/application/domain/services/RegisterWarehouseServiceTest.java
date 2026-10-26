package application.domain.services;

import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterWarehouseServiceTest {

    private final InMemoryRepositories.Warehouses repo = new InMemoryRepositories.Warehouses();
    private final RegisterWarehouseService service =
            new RegisterWarehouseService(new AuthorizationService(), repo);

    private final User admin = Fixtures.user(UserRole.ADMIN);

    @Test
    void adminRegistersAMarketplaceWarehouse() {
        Warehouse warehouse = service.registerForMarketplace(admin, "Central", WarehouseType.PHYSICAL,
                Fixtures.address());

        assertTrue(warehouse.isMarketplaceOwned());
        assertEquals(1, repo.saved.size());
    }

    @Test
    void adminRegistersASellerWarehouse() {
        Seller seller = Fixtures.seller(Fixtures.user(UserRole.SELLER));

        Warehouse warehouse = service.registerForSeller(admin, seller, "North warehouse",
                WarehouseType.PHYSICAL, Fixtures.address());

        assertTrue(warehouse.belongsTo(seller.getId()));
    }

    @Test
    void rejectsADuplicateWarehouseName() {
        service.registerForMarketplace(admin, "Central", WarehouseType.PHYSICAL, Fixtures.address());

        assertThrows(IllegalStateException.class,
                () -> service.registerForMarketplace(admin, "central", WarehouseType.PHYSICAL,
                        Fixtures.address()));
        assertEquals(1, repo.saved.size());
    }

    @Test
    void sellerDoesNotRegisterWarehouses() {
        User user = Fixtures.user(UserRole.SELLER);

        assertThrows(AccessDeniedException.class,
                () -> service.registerForSeller(user, Fixtures.seller(user), "Mine",
                        WarehouseType.PHYSICAL, Fixtures.address()));
    }

    @Test
    void physicalWarehouseWithoutAddressIsNotSaved() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registerForMarketplace(admin, "Central", WarehouseType.PHYSICAL, null));
        assertEquals(0, repo.saved.size());
    }
}