package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import application.adapters.rest.dtos.requests.RegisterUserRequestDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;

/** Builds new domain users from requests and turns users into responses. */
public final class UserRestMapper {

    private UserRestMapper() {
    }

    public static User toDomain(RegisterUserRequestDTO dto) {
        return new User(new FullName(dto.firstNames(), dto.lastNames()), new Email(dto.email()),
                new IdentityDocument(dto.document()), dto.role());
    }

    public static User toDomain(RegisterBuyerRequestDTO dto) {
        return new User(new FullName(dto.firstNames(), dto.lastNames()), new Email(dto.email()),
                new IdentityDocument(dto.document()), UserRole.BUYER);
    }

    public static UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(user.getId(), user.getFullName().firstNames(), user.getFullName().lastNames(),
                user.getEmail().value(), user.getDocument().value(), user.getRole(), user.getStatus());
    }
}