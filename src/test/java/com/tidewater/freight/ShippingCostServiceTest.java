package com.tidewater.freight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShippingCostServiceTest {

    @Mock ZoneRateRepository repository;
    @Mock ZoneRateRule rule;

    private ShippingCostService service;

    @BeforeEach
    void setUp() {
        service = new ShippingCostService(repository, new HeavyItemSurcharge());
    }

    @Test
    void returnsRuleCostForLightStandardShipment() {
        ShipmentRequest request = request(Zone.DOMESTIC, "10", ServiceLevel.STANDARD);
        when(repository.findRule(Zone.DOMESTIC)).thenReturn(rule);
        when(rule.baseCost(request.weightKg())).thenReturn(new BigDecimal("50"));

        assertEquals(new BigDecimal("50.00"), service.calculate(request));
    }

    @Test
    void addsHeavySurchargeAboveThirtyKg() {
        ShipmentRequest request = request(Zone.DOMESTIC, "31", ServiceLevel.STANDARD);
        when(repository.findRule(Zone.DOMESTIC)).thenReturn(rule);
        when(rule.baseCost(request.weightKg())).thenReturn(new BigDecimal("100"));

        assertEquals(new BigDecimal("120.00"), service.calculate(request));
    }

    @Test
    void appliesServiceLevelAfterSurcharge() {
        // (100 + 20) * 1.5 = 180, not 100 * 1.5 + 20 = 170
        ShipmentRequest request = request(Zone.DOMESTIC, "31", ServiceLevel.EXPRESS);
        when(repository.findRule(Zone.DOMESTIC)).thenReturn(rule);
        when(rule.baseCost(request.weightKg())).thenReturn(new BigDecimal("100"));

        assertEquals(new BigDecimal("180.00"), service.calculate(request));
    }

    @Test
    void roundsHalfUpToTwoDecimals() {
        ShipmentRequest request = request(Zone.DOMESTIC, "1", ServiceLevel.STANDARD);
        when(repository.findRule(Zone.DOMESTIC)).thenReturn(rule);
        when(rule.baseCost(request.weightKg())).thenReturn(new BigDecimal("1.665"));

        assertEquals(new BigDecimal("1.67"), service.calculate(request));
    }

    @Test
    void unknownZoneFailsLoudlyAndNeverReturnsZero() {
        ShipmentRequest request = request(Zone.REGIONAL, "10", ServiceLevel.STANDARD);
        UnknownZoneException failure = new UnknownZoneException(Zone.REGIONAL);
        when(repository.findRule(Zone.REGIONAL)).thenThrow(failure);

        UnknownZoneException thrown =
                assertThrows(UnknownZoneException.class, () -> service.calculate(request));

        assertSame(failure, thrown);
        assertEquals("No rate rule configured for zone: REGIONAL", thrown.getMessage());
        verify(rule, never()).baseCost(request.weightKg());
    }

    @Test
    void rejectsNullRequest() {
        InvalidShipmentException thrown =
                assertThrows(InvalidShipmentException.class, () -> service.calculate(null));

        assertEquals("Shipment request is required", thrown.getMessage());
    }

    @Test
    void realRulesGiveTheOriginalInternationalExpressPrice() {
        // sociable: real repository + real rule. 15*40 + 25 = 625; +20 = 645; *1.5 = 967.50
        ShippingCostService real = new ShippingCostService(
                new InMemoryZoneRateRepository(Map.of(Zone.INTERNATIONAL,
                        new FlatRateZoneRule(new BigDecimal("15"), new BigDecimal("25")))),
                new HeavyItemSurcharge());

        BigDecimal cost = real.calculate(request(Zone.INTERNATIONAL, "40", ServiceLevel.EXPRESS));

        assertEquals(new BigDecimal("967.50"), cost);
    }

    private static ShipmentRequest request(Zone zone, String weightKg, ServiceLevel level) {
        return new ShipmentRequest(zone, new BigDecimal(weightKg), level);
    }
}