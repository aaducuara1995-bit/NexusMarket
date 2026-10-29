package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/** First stock of a physical product in a warehouse. */
public record InitialInventoryRequestDTO(
        @NotNull UUID productId,
        @NotNull UUID warehouseId,
        @NotNull @PositiveOrZero Integer quantity) {
}