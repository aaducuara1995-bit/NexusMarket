package application.adapters.rest.dtos.requests;

import application.adapters.rest.dtos.AddressDTO;
import application.domain.enums.WarehouseType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** A new warehouse. A DIGITAL warehouse has no address; a PHYSICAL one needs it. */
public record WarehouseRequestDTO(
        @NotBlank String name,
        @NotNull WarehouseType type,
        @Valid AddressDTO address) {
}