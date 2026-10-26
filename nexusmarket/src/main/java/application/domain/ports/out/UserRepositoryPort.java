package application.domain.ports.out;

import application.domain.entities.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    boolean existsByEmail(Email email);

    boolean existsByDocument(IdentityDocument document);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    List<User> findAll();

    void save(User user);
}