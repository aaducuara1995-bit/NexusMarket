package application.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Dinero(BigDecimal monto, String moneda) {

    public Dinero {
        if (monto == null)
            throw new IllegalArgumentException("El monto es obligatorio.");
        if (monto.signum() < 0)
            throw new IllegalArgumentException("El monto no puede ser negativo.");
        if (moneda == null || moneda.isBlank())
            throw new IllegalArgumentException("La moneda es obligatoria.");

        monto = monto.setScale(2, RoundingMode.HALF_UP);
        moneda = moneda.trim().toUpperCase();
    }

    public Dinero(BigDecimal monto) {
        this(monto, "COP");
    }
}