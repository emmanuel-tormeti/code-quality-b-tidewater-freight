package com.tidewater.freight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HeavyItemSurchargeTest {

    private final HeavyItemSurcharge surcharge = new HeavyItemSurcharge();

    @ParameterizedTest(name = "{0}kg on cost {1} gives {2}")
    @CsvSource({
            "29.99, 100, 100",
            "30,    100, 100",      // boundary: exactly 30kg is NOT heavy
            "30.01, 100, 120",
            "31,    50,  70"
    })
    void addsTwentyOnlyStrictlyAboveThirtyKg(String weight, String cost, String expected) {
        BigDecimal result = surcharge.applyTo(new BigDecimal(weight), new BigDecimal(cost));

        assertEquals(0, new BigDecimal(expected).compareTo(result),
                "expected " + expected + " but was " + result);
    }
}