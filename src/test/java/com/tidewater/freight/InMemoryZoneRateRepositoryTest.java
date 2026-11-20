package com.tidewater.freight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryZoneRateRepositoryTest {

    private final ZoneRateRule domestic = new FlatRateZoneRule(BigDecimal.ONE, BigDecimal.ZERO);

    @Test
    void returnsTheRuleConfiguredForTheZone() {
        InMemoryZoneRateRepository repository =
                new InMemoryZoneRateRepository(Map.of(Zone.DOMESTIC, domestic));

        assertSame(domestic, repository.findRule(Zone.DOMESTIC));
    }

    @Test
    void unknownZoneThrowsWithZoneNamedInMessage() {
        InMemoryZoneRateRepository repository =
                new InMemoryZoneRateRepository(Map.of(Zone.DOMESTIC, domestic));

        UnknownZoneException thrown =
                assertThrows(UnknownZoneException.class, () -> repository.findRule(Zone.REGIONAL));

        assertEquals("No rate rule configured for zone: REGIONAL", thrown.getMessage());
    }

    @Test
    void nullZoneIsTreatedAsUnknown() {
        InMemoryZoneRateRepository repository = new InMemoryZoneRateRepository(Map.of());

        UnknownZoneException thrown =
                assertThrows(UnknownZoneException.class, () -> repository.findRule(null));

        assertEquals("No rate rule configured for zone: null", thrown.getMessage());
    }

    @Test
    void laterChangesToTheSuppliedMapDoNotLeakIn() {
        Map<Zone, ZoneRateRule> source = new EnumMap<>(Zone.class);
        source.put(Zone.DOMESTIC, domestic);
        InMemoryZoneRateRepository repository = new InMemoryZoneRateRepository(source);

        source.put(Zone.REGIONAL, domestic);

        assertThrows(UnknownZoneException.class, () -> repository.findRule(Zone.REGIONAL));
    }
}