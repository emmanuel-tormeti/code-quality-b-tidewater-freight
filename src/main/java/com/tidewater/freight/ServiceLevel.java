package com.tidewater.freight;

import java.math.BigDecimal;

public enum ServiceLevel {
    STANDARD("1.0"),
    EXPRESS("1.5");

    private final BigDecimal multiplier;

    ServiceLevel(String multiplier) {
        this.multiplier = new BigDecimal(multiplier);
    }

    public BigDecimal applyTo(BigDecimal cost) {
        return cost.multiply(multiplier);
    }
}
