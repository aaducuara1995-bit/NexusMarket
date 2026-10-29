package application.adapters.rest.dtos;

import jakarta.validation.constraints.NotBlank;

/** Address used in requests and responses. */
public record AddressDTO(
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String department,
        @NotBlank String postalCode) {
}