package application.domain.services;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterUserServiceTest {

    private final InMemoryRepositories.Users repo = new InMemoryRepositories.Users();
    private final RegisterUserService service =
            new RegisterUserService(new AuthorizationService(), repo);

    private final FullName name = new FullName("Luis", "Gomez");
    private final User admin = Fixtures.user(UserRole.ADMIN);

    private Email email(String value) {
        return new Email(value);
    }

    private IdentityDocument document(String value) {
        return new IdentityDocument(value);
    }

    @Test
    void buyerSelfRegistersAndIsSaved() {
        User user = service.register(null, name, email("luis@mail.com"),
                document("1001"), UserRole.BUYER);

        assertEquals(UserRole.BUYER, user.getRole());
        assertEquals(1, repo.saved.size());
    }

    @Test
    void rejectsADuplicateEmail() {
        service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER);

        assertThrows(IllegalStateException.class,
                () -> service.register(null, name, email("luis@mail.com"),
                        document("2002"), UserRole.BUYER));
        assertEquals(1, repo.saved.size());
    }

    @Test
    void emailIsComparedIgnoringCase() {
        service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER);

        assertThrows(IllegalStateException.class,
                () -> service.register(null, name, email("LUIS@Mail.com"),
                        document("2002"), UserRole.BUYER));
    }

    @Test
    void rejectsADuplicateDocument() {
        service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER);

        assertThrows(IllegalStateException.class,
                () -> service.register(null, name, email("other@mail.com"),
                        document("1001"), UserRole.BUYER));
    }

    @Test
    void sellerCannotSelfRegister() {
        assertThrows(AccessDeniedException.class,
                () -> service.register(null, name, email("s@mail.com"),
                        document("3003"), UserRole.SELLER));
    }

    @Test
    void adminRegistersASeller() {
        User seller = service.register(admin, name, email("s@mail.com"),
                document("3003"), UserRole.SELLER);

        assertEquals(UserRole.SELLER, seller.getRole());
    }

    @Test
    void buyerCannotRegisterSellers() {
        User buyer = Fixtures.user(UserRole.BUYER);

        assertThrows(AccessDeniedException.class,
                () -> service.register(buyer, name, email("s@mail.com"),
                        document("3003"), UserRole.SELLER));
    }

    @Test
    void onlyAnAdminRegistersLogisticOperators() {
        assertThrows(AccessDeniedException.class,
                () -> service.register(null, name, email("op@mail.com"),
                        document("4004"), UserRole.LOGISTIC_OPERATOR));

        User operator = service.register(admin, name, email("op@mail.com"),
                document("4004"), UserRole.LOGISTIC_OPERATOR);
        assertEquals(UserRole.LOGISTIC_OPERATOR, operator.getRole());
    }
}