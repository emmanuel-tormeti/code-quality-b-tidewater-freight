package com.tidewater.freight;

public enum Zone {
    DOMESTIC, REGIONAL, INTERNATIONAL;

    public static Zone parseZone(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new InvalidShipmentException("Unknown zone: " + name, e);
        }
    }
}