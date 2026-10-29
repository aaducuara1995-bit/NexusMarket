package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddToCartRequestDTO(
        @NotNull UUID productId,
        @NotNull @Positive Integer quantity) {
}