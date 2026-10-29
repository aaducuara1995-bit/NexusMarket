package application.domain.services;

import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;

public class RegisterUserService {

    private final AuthorizationService authorization;
    private final UserRepositoryPort users;
    private final PasswordServicePort passwords;

    public static final int MIN_PASSWORD_LENGTH = 8;

    public RegisterUserService(AuthorizationService authorization, UserRepositoryPort users,
                               PasswordServicePort passwords) {
        if (authorization == null || users == null || passwords == null)
            throw new IllegalArgumentException("Service dependencies are required.");
        this.authorization = authorization;
        this.users = users;
        this.passwords = passwords;
    }

    /**
     * A buyer can self-register (null actor). Sellers do not self-register:
     * an admin registers them, the same as the other internal roles.
     */
    public User register(User actor, FullName name, Email email,
                             IdentityDocument document, UserRole role, String rawPassword) {
        if (role == null)
            throw new IllegalArgumentException("Role is required.");

        if (role == UserRole.SELLER) {
            if (actor == null)
                throw new AccessDeniedException("Sellers cannot self-register.");
            authorization.requirePermission(actor, BusinessProcess.SELLER_REGISTRATION);
        } else if (role != UserRole.BUYER) {
            if (actor == null || !actor.hasRole(UserRole.ADMIN) || !actor.isActive())
                throw new AccessDeniedException("Only an active admin can register this role.");
        }

        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH)
            throw new IllegalArgumentException(
                    "The password must have at least " + MIN_PASSWORD_LENGTH + " characters.");

        // Email and document must be unique across the platform.
        if (email != null && users.existsByEmail(email))
            throw new IllegalStateException("A user with that email already exists.");
        if (document != null && users.existsByDocument(document))
            throw new IllegalStateException("A user with that identity document already exists.");

        User user = new User(name, email, document, role);
        user.assignPasswordHash(passwords.encrypt(rawPassword));
        users.save(user);
        return user;
    }
}