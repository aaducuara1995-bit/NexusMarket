package application.domain.services;

import application.domain.entities.User;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.TokenServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.AuthenticationResult;
import application.domain.valueobjects.Email;

/**
 * Authenticates a user: finds them by email, checks the password through the
 * PasswordServicePort, rejects blocked users and asks the TokenServicePort for the token.
 */
public class LoginService {

    private final UserRepositoryPort users;
    private final PasswordServicePort passwords;
    private final TokenServicePort tokens;

    public LoginService(UserRepositoryPort users, PasswordServicePort passwords, TokenServicePort tokens) {
        if (users == null || passwords == null || tokens == null)
            throw new IllegalArgumentException("Service dependencies are required.");
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    public AuthenticationResult login(Email email, String rawPassword) {
        if (email == null || rawPassword == null || rawPassword.isBlank())
            throw new InvalidCredentialsException();
        User user = users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (!user.hasPassword() || !passwords.matches(rawPassword, user.getPasswordHash()))
            throw new InvalidCredentialsException();
        if (!user.isActive())
            throw new AccessDeniedException("The user is blocked and cannot sign in.");
        return new AuthenticationResult(user, tokens.generateToken(user));
    }
}