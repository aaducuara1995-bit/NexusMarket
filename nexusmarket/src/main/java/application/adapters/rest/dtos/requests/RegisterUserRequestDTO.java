package application.adapters.rest.dtos.requests;

import application.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** A user registered by the administrator, with any role and an initial password. */
public record RegisterUserRequestDTO(
        @NotBlank String firstNames,
        @NotBlank String lastNames,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d+", message = "must contain only digits") String document,
        @NotBlank @Size(min = 8, message = "must have at least 8 characters") String password,
        @NotNull UserRole role) {
}