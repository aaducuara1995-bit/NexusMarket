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
            new RegisterUserService(new AuthorizationService(), repo, new InMemoryRepositories.FakePasswords());

    private static final String PASSWORD = "secret123";

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
                document("1001"), UserRole.BUYER, PASSWORD);

        assertEquals(UserRole.BUYER, user.getRole());
        assertEquals(1, repo.saved.size());
    }

    @Test
    void rejectsADuplicateEmail() {
        service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER, PASSWORD);

        assertThrows(IllegalStateException.class,
                () -> service.register(null, name, email("luis@mail.com"),
                        document("2002"), UserRole.BUYER, PASSWORD));
        assertEquals(1, repo.saved.size());
    }

    @Test
    void emailIsComparedIgnoringCase() {
        service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER, PASSWORD);

        assertThrows(IllegalStateException.class,
                () -> service.register(null, name, email("LUIS@Mail.com"),
                        document("2002"), UserRole.BUYER, PASSWORD));
    }

    @Test
    void rejectsADuplicateDocument() {
        service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER, PASSWORD);

        assertThrows(IllegalStateException.class,
                () -> service.register(null, name, email("other@mail.com"),
                        document("1001"), UserRole.BUYER, PASSWORD));
    }

    @Test
    void sellerCannotSelfRegister() {
        assertThrows(AccessDeniedException.class,
                () -> service.register(null, name, email("s@mail.com"),
                        document("3003"), UserRole.SELLER, PASSWORD));
    }

    @Test
    void adminRegistersASeller() {
        User seller = service.register(admin, name, email("s@mail.com"),
                document("3003"), UserRole.SELLER, PASSWORD);

        assertEquals(UserRole.SELLER, seller.getRole());
    }

    @Test
    void buyerCannotRegisterSellers() {
        User buyer = Fixtures.user(UserRole.BUYER);

        assertThrows(AccessDeniedException.class,
                () -> service.register(buyer, name, email("s@mail.com"),
                        document("3003"), UserRole.SELLER, PASSWORD));
    }

    @Test
    void onlyAnAdminRegistersLogisticOperators() {
        assertThrows(AccessDeniedException.class,
                () -> service.register(null, name, email("op@mail.com"),
                        document("4004"), UserRole.LOGISTIC_OPERATOR, PASSWORD));

        User operator = service.register(admin, name, email("op@mail.com"),
                document("4004"), UserRole.LOGISTIC_OPERATOR, PASSWORD);
        assertEquals(UserRole.LOGISTIC_OPERATOR, operator.getRole());
    }

    @Test
    void thePasswordIsStoredOnlyAsAHash() {
        User user = service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER, PASSWORD);

        assertEquals("hash:" + PASSWORD, user.getPasswordHash());
    }

    @Test
    void aShortPasswordIsRejectedAndNothingIsSaved() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER, "1234567"));
        assertThrows(IllegalArgumentException.class,
                () -> service.register(null, name, email("luis@mail.com"), document("1001"), UserRole.BUYER, null));
        assertEquals(0, repo.saved.size());
    }
}