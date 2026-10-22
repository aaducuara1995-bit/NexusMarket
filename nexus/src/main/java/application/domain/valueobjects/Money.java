package application.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null)
            throw new IllegalArgumentException("Amount is required.");
        if (amount.signum() < 0)
            throw new IllegalArgumentException("Amount cannot be negative.");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Currency is required.");

        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency.trim().toUpperCase();
    }

    public Money(BigDecimal amount) {
        this(amount, "COP");
    }
}