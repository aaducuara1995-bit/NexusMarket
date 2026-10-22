package application.domain.services;

import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterSellerServiceTest {

    private final InMemoryRepositories.Sellers repo = new InMemoryRepositories.Sellers();
    private final RegisterSellerService service =
            new RegisterSellerService(new AuthorizationService(), repo);

    private final User admin = Fixtures.user(UserRole.ADMIN);

    @Test
    void adminRegistersTheSellerProfile() {
        User user = Fixtures.user(UserRole.SELLER);

        Seller seller = service.register(admin, user);

        assertEquals(user.getId(), seller.getUserId());
        assertEquals(1, repo.saved.size());
    }

    @Test
    void userCannotHaveTwoSellerProfiles() {
        User user = Fixtures.user(UserRole.SELLER);
        service.register(admin, user);

        assertThrows(IllegalStateException.class, () -> service.register(admin, user));
        assertEquals(1, repo.saved.size());
    }

    @Test
    void onlyAnAdminRegistersSellers() {
        User otherSeller = Fixtures.user(UserRole.SELLER);

        assertThrows(AccessDeniedException.class,
                () -> service.register(otherSeller, Fixtures.user(UserRole.SELLER)));
    }

    @Test
    void rejectsAUserWithAnotherRole() {
        assertThrows(IllegalStateException.class,
                () -> service.register(admin, Fixtures.user(UserRole.BUYER)));
    }

    @Test
    void rejectsABlockedUser() {
        User user = Fixtures.user(UserRole.SELLER);
        user.block();

        assertThrows(IllegalStateException.class, () -> service.register(admin, user));
    }
}