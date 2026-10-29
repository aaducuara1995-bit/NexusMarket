package application.domain.entities;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;

import java.util.UUID;

public class User {

    private final UUID id;
    private final FullName fullName;
    private final Email email;
    private final IdentityDocument document;
    private final UserRole role;
    private UserStatus status;
    private String passwordHash;

    public User(FullName fullName,
                   Email email,
                   IdentityDocument document,
                   UserRole role) {
        if (fullName == null || email == null || document == null || role == null)
            throw new IllegalArgumentException("Name, email, document and role are required.");

        this.id = UUID.randomUUID();
        this.fullName = fullName;
        this.email = email;
        this.document = document;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    private User(UUID id, FullName fullName, Email email, IdentityDocument document,
                 UserRole role, UserStatus status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.document = document;
        this.role = role;
        this.status = status;
    }

    /**
     * Rebuilds a user without its password hash. The security adapter uses it to rebuild
     * the signed-in user from the JWT claims, which never carry the password.
     */
    public static User restore(UUID id, FullName fullName, Email email, IdentityDocument document,
                               UserRole role, UserStatus status) {
        Restore.require(id, "id");
        Restore.require(fullName, "fullName");
        Restore.require(email, "email");
        Restore.require(document, "document");
        Restore.require(role, "role");
        Restore.require(status, "status");
        return new User(id, fullName, email, document, role, status);
    }

    /** Rebuilds a user saved by a persistence adapter, with its password hash (null if it never had one). */
    public static User restore(UUID id, FullName fullName, Email email, IdentityDocument document,
                               UserRole role, UserStatus status, String passwordHash) {
        User user = restore(id, fullName, email, document, role, status);
        user.passwordHash = passwordHash;
        return user;
    }

    /**
     * Stores the password already hashed by the PasswordServicePort. The domain never
     * sees nor keeps the password in plain text.
     */
    public void assignPasswordHash(String hash) {
        if (hash == null || hash.isBlank())
            throw new IllegalArgumentException("The password hash is required.");
        this.passwordHash = hash;
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }

    public void block() {
        if (status == UserStatus.BLOCKED)
            throw new IllegalStateException("The user is already blocked.");
        status = UserStatus.BLOCKED;
    }

    public void activate() {
        if (status == UserStatus.ACTIVE)
            throw new IllegalStateException("The user is already active.");
        status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean hasRole(UserRole expected) {
        return role == expected;
    }

    public UUID getId() { return id; }
    public FullName getFullName() { return fullName; }
    public Email getEmail() { return email; }
    public IdentityDocument getDocument() { return document; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public String getPasswordHash() { return passwordHash; }
}