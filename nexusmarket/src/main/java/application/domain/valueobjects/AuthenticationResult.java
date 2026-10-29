package application.domain.valueobjects;

import application.domain.entities.User;

/** Result of a successful login: the authenticated user and the token that identifies them. */
public record AuthenticationResult(User user, String token) {

    public AuthenticationResult {
        if (user == null)
            throw new IllegalArgumentException("The authenticated user is required.");
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("The token is required.");
    }
}