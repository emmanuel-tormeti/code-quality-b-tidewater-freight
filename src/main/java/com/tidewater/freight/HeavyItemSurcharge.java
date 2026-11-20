package com.tidewater.freight;

import java.math.BigDecimal;

public class HeavyItemSurcharge {

    private static final BigDecimal THRESHOLD_KG = new BigDecimal("30");
    private static final BigDecimal AMOUNT = new BigDecimal("20");

    public BigDecimal applyTo(BigDecimal weightKg, BigDecimal cost) {
        return weightKg.compareTo(THRESHOLD_KG) > 0 ? cost.add(AMOUNT) : cost;
    }
}