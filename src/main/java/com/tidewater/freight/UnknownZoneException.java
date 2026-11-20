package com.tidewater.freight;

public class UnknownZoneException extends ShippingException {
    public UnknownZoneException(Zone zone) {
        super("No rate rule configured for zone: " + zone);
    }
}