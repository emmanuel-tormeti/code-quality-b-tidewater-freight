package com.tidewater.freight;

public enum Zone {
    DOMESTIC, REGIONAL, INTERNATIONAL;

    public static Zone parseZone(String name) {
        if (name == null) {
            throw new InvalidShipmentException("Zone name is required");
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new InvalidShipmentException("Unknown zone: " + name, e);
        }
    }
}
