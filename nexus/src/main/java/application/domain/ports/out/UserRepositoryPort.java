package application.domain.ports.out;

import application.domain.entities.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;

public interface UserRepositoryPort {

    boolean existsByEmail(Email email);

    boolean existsByDocument(IdentityDocument document);

    void save(User user);
}