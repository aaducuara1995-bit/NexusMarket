package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/** Units of an approved return that go back to stock. */
public record RestockRequestDTO(
        @NotNull UUID returnId,
        @NotNull @Positive Integer units) {
}