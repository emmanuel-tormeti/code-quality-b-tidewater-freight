package com.tidewater.freight;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Supplier;

public class Demo {

    public static void main(String[] args) {
        ShippingCostService service = new ShippingCostService(
                new InMemoryZoneRateRepository(Map.of(
                        Zone.DOMESTIC, rule("5", "0"),
                        Zone.REGIONAL, rule("8", "10"),
                        Zone.INTERNATIONAL, rule("15", "25"))),
                new HeavyItemSurcharge());
        ShippingCostService domesticOnly = new ShippingCostService(
                new InMemoryZoneRateRepository(Map.of(Zone.DOMESTIC, rule("5", "0"))),
                new HeavyItemSurcharge());

        show("DOMESTIC 10kg STANDARD",
                () -> service.calculate(request(Zone.DOMESTIC, "10", ServiceLevel.STANDARD)));
        show("INTERNATIONAL 40kg EXPRESS",
                () -> service.calculate(request(Zone.INTERNATIONAL, "40", ServiceLevel.EXPRESS)));
        show("REGIONAL with no rule configured",
                () -> domesticOnly.calculate(request(Zone.REGIONAL, "10", ServiceLevel.STANDARD)));
        show("DOMESTIC -5kg STANDARD",
                () -> service.calculate(request(Zone.DOMESTIC, "-5", ServiceLevel.STANDARD)));
    }

    private static void show(String label, Supplier<BigDecimal> calculation) {
        try {
            System.out.println("Result " + label + " -> GHS " + calculation.get());
        } catch (ShippingException e) {
            System.err.println("Result " + label + " -> REJECTED: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("  caused by: " + e.getCause());
            }
        }
    }

    private static ZoneRateRule rule(String ratePerKg, String handlingFee) {
        return new FlatRateZoneRule(new BigDecimal(ratePerKg), new BigDecimal(handlingFee));
    }

    private static ShipmentRequest request(Zone zone, String weightKg, ServiceLevel level) {
        return new ShipmentRequest(zone, new BigDecimal(weightKg), level);
    }
}
