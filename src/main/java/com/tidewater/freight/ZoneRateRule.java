package com.tidewater.freight;

import java.math.BigDecimal;

public interface ZoneRateRule {
    BigDecimal baseCost(BigDecimal weightKg);
}