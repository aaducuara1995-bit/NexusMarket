package application.adapters.rest.dtos.requests;

import application.domain.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/** A new product of the signed-in seller. Currency is optional (COP by default). */
public record ProductRequestDTO(
        @NotBlank String name,
        String description,
        @NotNull ProductType type,
        @NotNull @Positive BigDecimal price,
        String currency,
        List<String> variants) {
}