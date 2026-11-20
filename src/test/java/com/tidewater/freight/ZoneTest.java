package com.tidewater.freight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ZoneTest {

    @Test
    void parsesAKnownZoneName() {
        assertEquals(Zone.REGIONAL, Zone.parseZone("REGIONAL"));
    }

    @Test
    void unknownNameThrowsInvalidShipmentExceptionKeepingTheCause() {
        InvalidShipmentException thrown =
                assertThrows(InvalidShipmentException.class, () -> Zone.parseZone("MOON"));

        assertEquals("Unknown zone: MOON", thrown.getMessage());
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
    }

    @Test
    void nullNameThrowsInvalidShipmentException() {
        InvalidShipmentException thrown =
                assertThrows(InvalidShipmentException.class, () -> Zone.parseZone(null));
    
        assertEquals("Zone name is required", thrown.getMessage());
    }
}