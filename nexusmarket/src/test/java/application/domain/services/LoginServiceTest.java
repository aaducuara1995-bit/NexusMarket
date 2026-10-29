package application.domain.services;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.valueobjects.AuthenticationResult;
import application.domain.valueobjects.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {

    private final InMemoryRepositories.Users users = new InMemoryRepositories.Users();
    private final LoginService service = new LoginService(users,
            new InMemoryRepositories.FakePasswords(), new InMemoryRepositories.FakeTokens());

    private User registered(String password) {
        User user = Fixtures.user(UserRole.SELLER);
        user.assignPasswordHash("hash:" + password);
        users.save(user);
        return user;
    }

    @Test
    void theRightPasswordReturnsTheUserAndItsToken() {
        User user = registered("secret123");

        AuthenticationResult result = service.login(new Email("ANA@mail.com"), "secret123");

        assertSame(user, result.user());
        assertEquals("token-" + user.getId(), result.token());
    }

    @Test
    void aWrongPasswordAndAnUnknownEmailGetTheSameAnswer() {
        registered("secret123");

        InvalidCredentialsException wrongPassword = assertThrows(InvalidCredentialsException.class,
                () -> service.login(new Email("ana@mail.com"), "other-password"));
        InvalidCredentialsException unknownEmail = assertThrows(InvalidCredentialsException.class,
                () -> service.login(new Email("nobody@mail.com"), "secret123"));
        assertEquals(wrongPassword.getMessage(), unknownEmail.getMessage());
    }

    @Test
    void aUserWithoutPasswordCannotSignIn() {
        users.save(Fixtures.user(UserRole.BUYER));

        assertThrows(InvalidCredentialsException.class, () -> service.login(new Email("ana@mail.com"), "anything1"));
        assertThrows(InvalidCredentialsException.class, () -> service.login(new Email("ana@mail.com"), " "));
    }

    @Test
    void aBlockedUserCannotSignInEvenWithTheRightPassword() {
        registered("secret123").block();

        assertThrows(AccessDeniedException.class, () -> service.login(new Email("ana@mail.com"), "secret123"));
    }
}