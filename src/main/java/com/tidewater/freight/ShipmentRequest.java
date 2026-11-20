package com.tidewater.freight;

import java.math.BigDecimal;

public record ShipmentRequest(Zone zone, BigDecimal weightKg, ServiceLevel serviceLevel) {

    public ShipmentRequest {
        if (zone == null) {
            throw new InvalidShipmentException("Zone is required");
        }
        if (weightKg == null) {
            throw new InvalidShipmentException("Weight is required");
        }
        if (serviceLevel == null) {
            throw new InvalidShipmentException("Service level is required");
        }
        if (weightKg.signum() <= 0) {
            throw new InvalidShipmentException("Weight must be greater than zero: " + weightKg);
        }
    }
}