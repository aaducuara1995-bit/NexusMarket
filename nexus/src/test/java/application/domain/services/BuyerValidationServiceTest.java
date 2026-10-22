package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuyerValidationServiceTest {

    private final BuyerValidationService service = new BuyerValidationService();

    private User user(UserRole role) {
        return new User(
                new FullName("Ana", "Perez"),
                new Email("ana@mail.com"),
                new IdentityDocument("123456"),
                role);
    }

    private Buyer buyer(User user) {
        return new Buyer(user.getId(),
                new Address("Calle 1 # 2-3", "Medellin", "Antioquia", "050001"));
    }

    @Test
    void enabledBuyerCanPurchase() {
        User user = user(UserRole.BUYER);
        Buyer buyer = buyer(user);

        assertTrue(service.canPurchase(user, buyer));
        assertDoesNotThrow(() -> service.requireEnabled(user, buyer));
    }

    @Test
    void restrictedBuyerCannotPurchase() {
        User user = user(UserRole.BUYER);
        Buyer buyer = buyer(user);
        buyer.restrict();

        assertFalse(service.canPurchase(user, buyer));
        assertThrows(IllegalStateException.class, () -> service.requireEnabled(user, buyer));
    }

    @Test
    void blockedUserCannotPurchase() {
        User user = user(UserRole.BUYER);
        Buyer buyer = buyer(user);
        user.block();

        assertFalse(service.canPurchase(user, buyer));
        assertThrows(IllegalStateException.class, () -> service.requireEnabled(user, buyer));
    }

    @Test
    void userWithAnotherRoleCannotPurchase() {
        User seller = user(UserRole.SELLER);
        Buyer buyer = buyer(seller);

        assertFalse(service.canPurchase(seller, buyer));
        assertThrows(AccessDeniedException.class, () -> service.requireEnabled(seller, buyer));
    }

    @Test
    void buyerMustMatchTheUser() {
        User user = user(UserRole.BUYER);
        Buyer fromAnotherPerson = buyer(user(UserRole.BUYER));

        assertFalse(service.canPurchase(user, fromAnotherPerson));
        assertThrows(AccessDeniedException.class, () -> service.requireEnabled(user, fromAnotherPerson));
    }
}