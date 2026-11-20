package com.tidewater.freight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServiceLevelTest {

    @ParameterizedTest(name = "{0} on {1} gives {2}")
    @CsvSource({
            "STANDARD, 100, 100",
            "EXPRESS,  100, 150",
            "EXPRESS,  0,   0"
    })
    void appliesItsMultiplier(ServiceLevel level, String cost, String expected) {
        BigDecimal result = level.applyTo(new BigDecimal(cost));

        assertEquals(0, new BigDecimal(expected).compareTo(result),
                "expected " + expected + " but was " + result);
    }
}