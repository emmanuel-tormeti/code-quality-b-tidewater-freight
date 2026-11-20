package com.tidewater.freight;

import java.math.BigDecimal;

public enum ServiceLevel {
    STANDARD(new BigDecimal("1.0")),
    EXPRESS(new BigDecimal("1.5"));

    private final BigDecimal multiplier;

    ServiceLevel(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal applyTo(BigDecimal cost) {
        return cost.multiply(multiplier);
    }
}
