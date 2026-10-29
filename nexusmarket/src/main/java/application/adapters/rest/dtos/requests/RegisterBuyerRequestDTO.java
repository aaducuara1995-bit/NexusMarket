package application.adapters.rest.dtos.requests;

import application.adapters.rest.dtos.AddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Public sign-up of a buyer: personal data and main address. */
public record RegisterBuyerRequestDTO(
        @NotBlank String firstNames,
        @NotBlank String lastNames,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d+", message = "must contain only digits") String document,
        @NotNull @Valid AddressDTO mainAddress) {
}