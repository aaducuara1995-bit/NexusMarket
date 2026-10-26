package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.domain.entities.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper between the User domain entity and its JPA entity. */
public final class UserJpaMapper {

    private UserJpaMapper() {
    }

    public static UserJpaEntity toEntity(User domain) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setFirstNames(domain.getFullName().firstNames());
        entity.setLastNames(domain.getFullName().lastNames());
        entity.setEmail(domain.getEmail().value());
        entity.setDocument(domain.getDocument().value());
        entity.setRole(domain.getRole());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    public static User toDomain(UserJpaEntity entity) {
        return User.restore(uuid(entity.getId()),
                new FullName(entity.getFirstNames(), entity.getLastNames()),
                new Email(entity.getEmail()),
                new IdentityDocument(entity.getDocument()),
                entity.getRole(), entity.getStatus());
    }
}