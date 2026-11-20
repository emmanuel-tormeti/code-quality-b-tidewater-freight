package com.tidewater.freight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlatRateZoneRuleTest {

    @ParameterizedTest(name = "rate {0}, fee {1}, {2}kg gives {3}")
    @CsvSource({
            "5,  0,  10, 50",    // DOMESTIC
            "8,  10, 5,  50",    // REGIONAL
            "15, 25, 40, 625"    // INTERNATIONAL
    })
    void chargesRatePerKgPlusHandlingFee(String rate, String fee, String weight, String expected) {
        FlatRateZoneRule rule = new FlatRateZoneRule(new BigDecimal(rate), new BigDecimal(fee));

        BigDecimal result = rule.baseCost(new BigDecimal(weight));

        assertEquals(0, new BigDecimal(expected).compareTo(result),
                "expected " + expected + " but was " + result);
    }

    @Test
    void rejectsMissingRateOrFee() {
        assertEquals("Rate and handling fee are required",
                assertThrows(InvalidRateRuleException.class,
                        () -> new FlatRateZoneRule(null, BigDecimal.ONE)).getMessage());
        assertEquals("Rate and handling fee are required",
                assertThrows(InvalidRateRuleException.class,
                        () -> new FlatRateZoneRule(BigDecimal.ONE, null)).getMessage());
    }

    @Test
    void rejectsNegativeRateOrFee() {
        assertEquals("Rate and handling fee must not be negative",
                assertThrows(InvalidRateRuleException.class,
                        () -> new FlatRateZoneRule(new BigDecimal("-1"), BigDecimal.ZERO))
                        .getMessage());
        assertEquals("Rate and handling fee must not be negative",
                assertThrows(InvalidRateRuleException.class,
                        () -> new FlatRateZoneRule(BigDecimal.ONE, new BigDecimal("-1")))
                        .getMessage());
    }
}