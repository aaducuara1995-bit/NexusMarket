package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** The amount must match the order total exactly. Currency is optional (COP by default). */
public record PaymentRequestDTO(
        @NotNull @Positive BigDecimal amount,
        String currency) {
}