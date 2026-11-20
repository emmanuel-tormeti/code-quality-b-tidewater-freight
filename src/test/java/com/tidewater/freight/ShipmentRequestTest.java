package com.tidewater.freight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ShipmentRequestTest {

    private static final BigDecimal WEIGHT = new BigDecimal("1");

    @Test
    void acceptsAValidRequest() {
        ShipmentRequest request = new ShipmentRequest(Zone.DOMESTIC, WEIGHT, ServiceLevel.EXPRESS);

        assertEquals(Zone.DOMESTIC, request.zone());
        assertEquals(WEIGHT, request.weightKg());
        assertEquals(ServiceLevel.EXPRESS, request.serviceLevel());
    }

    @Test
    void rejectsMissingZone() {
        assertEquals("Zone is required", message(null, WEIGHT, ServiceLevel.STANDARD));
    }

    @Test
    void rejectsMissingWeight() {
        assertEquals("Weight is required", message(Zone.DOMESTIC, null, ServiceLevel.STANDARD));
    }

    @Test
    void rejectsMissingServiceLevel() {
        assertEquals("Service level is required", message(Zone.DOMESTIC, WEIGHT, null));
    }

    @ParameterizedTest(name = "weight {0} is rejected")
    @ValueSource(strings = {"0", "-0.01", "-5"})
    void rejectsZeroOrNegativeWeight(String weight) {
        assertEquals("Weight must be greater than zero: " + weight,
                message(Zone.DOMESTIC, new BigDecimal(weight), ServiceLevel.STANDARD));
    }

    @Test
    void acceptsSmallestPositiveWeight() {
        new ShipmentRequest(Zone.DOMESTIC, new BigDecimal("0.001"), ServiceLevel.STANDARD);
    }

    private static String message(Zone zone, BigDecimal weight, ServiceLevel level) {
        return assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest(zone, weight, level)).getMessage();
    }
}
