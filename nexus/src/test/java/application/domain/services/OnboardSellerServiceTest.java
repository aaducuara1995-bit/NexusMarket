package application.domain.services;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseOwnership;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnboardSellerServiceTest {

    private final OnboardSellerService service =
            new OnboardSellerService(new AuthorizationService());

    private User user(UserRole role) {
        return new User(
                new FullName("Ana", "Perez"),
                new Email("ana@mail.com"),
                new IdentityDocument("123456"),
                role);
    }

    private Address address() {
        return new Address("Calle 1 # 2-3", "Medellin", "Antioquia", "050001");
    }

    @Test
    void adminOnboardsASellerWithItsFirstWarehouse() {
        User admin = user(UserRole.ADMIN);
        User sellerUser = user(UserRole.SELLER);

        var result = service.onboard(admin, sellerUser,
                "Main warehouse", WarehouseType.PHYSICAL, address());

        assertEquals(sellerUser.getId(), result.seller().getUserId());
        assertTrue(result.warehouse().belongsTo(result.seller().getId()));
        assertEquals(WarehouseOwnership.SELLER, result.warehouse().getOwnership());
    }

    @Test
    void nonAdminCannotOnboardSellers() {
        User nonAdmin = user(UserRole.SELLER);
        User sellerUser = user(UserRole.SELLER);

        assertThrows(AccessDeniedException.class,
                () -> service.onboard(nonAdmin, sellerUser,
                        "Main warehouse", WarehouseType.PHYSICAL, address()));
    }

    @Test
    void onlyUsersWithSellerRoleAreOnboarded() {
        User admin = user(UserRole.ADMIN);
        User buyer = user(UserRole.BUYER);

        assertThrows(IllegalStateException.class,
                () -> service.onboard(admin, buyer,
                        "Main warehouse", WarehouseType.PHYSICAL, address()));
    }

    @Test
    void blockedUserIsNotOnboarded() {
        User admin = user(UserRole.ADMIN);
        User sellerUser = user(UserRole.SELLER);
        sellerUser.block();

        assertThrows(IllegalStateException.class,
                () -> service.onboard(admin, sellerUser,
                        "Main warehouse", WarehouseType.PHYSICAL, address()));
    }

    @Test
    void physicalWarehouseWithoutAddressIsRejected() {
        User admin = user(UserRole.ADMIN);
        User sellerUser = user(UserRole.SELLER);

        assertThrows(IllegalArgumentException.class,
                () -> service.onboard(admin, sellerUser,
                        "Main warehouse", WarehouseType.PHYSICAL, null));
    }
}