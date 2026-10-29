package application.adapters.rest.dtos.requests;

import application.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** A user registered by the administrator, with any role. */
public record RegisterUserRequestDTO(
        @NotBlank String firstNames,
        @NotBlank String lastNames,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d+", message = "must contain only digits") String document,
        @NotNull UserRole role) {
}