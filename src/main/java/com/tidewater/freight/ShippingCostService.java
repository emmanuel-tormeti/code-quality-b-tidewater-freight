package com.tidewater.freight;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ShippingCostService {

    private static final int MONEY_SCALE = 2;

    private final ZoneRateRepository rateRepository;
    private final HeavyItemSurcharge heavyItemSurcharge;

    public ShippingCostService(ZoneRateRepository rateRepository,
                               HeavyItemSurcharge heavyItemSurcharge) {
        this.rateRepository = rateRepository;
        this.heavyItemSurcharge = heavyItemSurcharge;
    }

    public BigDecimal calculate(ShipmentRequest request) {
        if (request == null) {
            throw new InvalidShipmentException("Shipment request is required");
        }
        ZoneRateRule rule = rateRepository.findRule(request.zone());
        BigDecimal cost = rule.baseCost(request.weightKg());
        cost = heavyItemSurcharge.applyTo(request.weightKg(), cost);
        cost = request.serviceLevel().applyTo(cost);
        return cost.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}