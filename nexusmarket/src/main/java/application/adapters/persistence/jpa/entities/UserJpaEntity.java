package application.adapters.persistence.jpa.entities;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity (repository DTO) for User, stored in MySQL table users.
 * User account. One role per user (RG-02); email and document are unique. The password is stored only as a BCrypt hash.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "first_names", length = 120, nullable = false)
    private String firstNames;

    @Column(name = "last_names", length = 120, nullable = false)
    private String lastNames;

    @Column(name = "email", length = 120, nullable = false, unique = true)
    private String email;

    @Column(name = "document", length = 30, nullable = false, unique = true)
    private String document;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private UserStatus status;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    public UserJpaEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getLastNames() {
        return lastNames;
    }

    public void setLastNames(String lastNames) {
        this.lastNames = lastNames;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}