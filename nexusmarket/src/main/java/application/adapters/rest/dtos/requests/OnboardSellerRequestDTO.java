package application.adapters.rest.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Turns a user with the SELLER role into a seller with their first warehouse (RN-01). */
public record OnboardSellerRequestDTO(
        @NotNull UUID userId,
        @NotNull @Valid WarehouseRequestDTO firstWarehouse) {
}