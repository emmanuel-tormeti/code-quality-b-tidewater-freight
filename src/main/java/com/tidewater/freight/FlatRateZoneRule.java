package com.tidewater.freight;

import java.math.BigDecimal;

public class FlatRateZoneRule implements ZoneRateRule {

    private final BigDecimal ratePerKg;
    private final BigDecimal handlingFee;

    public FlatRateZoneRule(BigDecimal ratePerKg, BigDecimal handlingFee) {
        if (ratePerKg == null || handlingFee == null) {
            throw new InvalidRateRuleException("Rate and handling fee are required");
        }
        if (ratePerKg.signum() < 0 || handlingFee.signum() < 0) {
            throw new InvalidRateRuleException("Rate and handling fee must not be negative");
        }
        this.ratePerKg = ratePerKg;
        this.handlingFee = handlingFee;
    }

    @Override
    public BigDecimal baseCost(BigDecimal weightKg) {
        return ratePerKg.multiply(weightKg).add(handlingFee);
    }
}
