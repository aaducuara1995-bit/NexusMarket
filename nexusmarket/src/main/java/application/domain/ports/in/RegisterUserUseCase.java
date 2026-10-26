package application.domain.ports.in;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;

/** Input port: registers a user with an email and document unique across the platform. */
public interface RegisterUserUseCase {

    User register(User actor, FullName name, Email email,
                      IdentityDocument document, UserRole role);
}