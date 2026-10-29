package application.adapters.rest;

import application.adapters.rest.exception.UnauthenticatedException;
import application.domain.entities.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.Email;
import org.springframework.stereotype.Component;

/**
 * Finds the domain User that makes a request. In phase C it reads the X-User-Email
 * header; in phase D this class rebuilds the User from the JWT instead, and the
 * controllers do not change.
 */
@Component
public class AuthenticatedUserResolver {

    public static final String HEADER = "X-User-Email";

    private final UserRepositoryPort users;

    public AuthenticatedUserResolver(UserRepositoryPort users) {
        this.users = users;
    }

    public User resolve(String email) {
        if (email == null || email.isBlank())
            throw new UnauthenticatedException("The " + HEADER + " header is required to identify the user.");
        Email parsed;
        try {
            parsed = new Email(email);
        } catch (IllegalArgumentException e) {
            throw new UnauthenticatedException("The " + HEADER + " header does not contain a valid email.");
        }
        return users.findByEmail(parsed)
                .orElseThrow(() -> new UnauthenticatedException("No user is registered with that email."));
    }
}