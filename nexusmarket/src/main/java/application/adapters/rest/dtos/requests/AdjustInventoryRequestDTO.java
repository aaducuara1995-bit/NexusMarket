package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Stock correction: a positive difference adds units, a negative one removes them. */
public record AdjustInventoryRequestDTO(
        @NotNull Integer difference,
        @NotBlank String reason) {
}