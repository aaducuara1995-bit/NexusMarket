package application.adapters.rest.dtos.responses;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;

import java.util.UUID;

public record UserResponseDTO(UUID id, String firstNames, String lastNames, String email,
                              String document, UserRole role, UserStatus status) {
}