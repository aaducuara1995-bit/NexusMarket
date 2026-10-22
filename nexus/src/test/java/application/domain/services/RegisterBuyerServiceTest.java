package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.User;
import application.domain.enums.CommercialStatus;
import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterBuyerServiceTest {

    private final InMemoryRepositories.Buyers repo = new InMemoryRepositories.Buyers();
    private final RegisterBuyerService service = new RegisterBuyerService(repo);

    @Test
    void createsAnEnabledBuyerProfile() {
        User user = Fixtures.user(UserRole.BUYER);

        Buyer buyer = service.register(user, Fixtures.address());

        assertEquals(user.getId(), buyer.getUserId());
        assertEquals(CommercialStatus.ENABLED, buyer.getCommercialStatus());
        assertEquals(1, repo.saved.size());
    }

    @Test
    void userCannotHaveTwoBuyerProfiles() {
        User user = Fixtures.user(UserRole.BUYER);
        service.register(user, Fixtures.address());

        assertThrows(IllegalStateException.class,
                () -> service.register(user, Fixtures.address()));
        assertEquals(1, repo.saved.size());
    }

    @Test
    void rejectsAUserWithAnotherRole() {
        User seller = Fixtures.user(UserRole.SELLER);

        assertThrows(IllegalStateException.class,
                () -> service.register(seller, Fixtures.address()));
    }

    @Test
    void rejectsABlockedUser() {
        User user = Fixtures.user(UserRole.BUYER);
        user.block();

        assertThrows(IllegalStateException.class,
                () -> service.register(user, Fixtures.address()));
    }

    @Test
    void requiresAMainAddress() {
        User user = Fixtures.user(UserRole.BUYER);

        assertThrows(IllegalArgumentException.class, () -> service.register(user, null));
        assertEquals(0, repo.saved.size());
    }
}