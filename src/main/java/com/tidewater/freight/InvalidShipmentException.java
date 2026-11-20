package com.tidewater.freight;

public class InvalidShipmentException extends ShippingException {
    public InvalidShipmentException(String message) {
        super(message);
    }

    public InvalidShipmentException(String message, Throwable cause) {
        super(message, cause);
    }
}