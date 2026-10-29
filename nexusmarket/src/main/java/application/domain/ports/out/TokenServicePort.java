package application.domain.ports.out;

import application.domain.entities.User;

/**
 * Issues the access token of an authenticated user. Implemented with JWT in the
 * security adapters; the domain does not know the token format.
 */
public interface TokenServicePort {
    String generateToken(User user);
}