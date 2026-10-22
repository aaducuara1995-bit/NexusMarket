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
}