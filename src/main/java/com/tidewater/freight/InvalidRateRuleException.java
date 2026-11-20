package com.tidewater.freight;

public class InvalidRateRuleException extends ShippingException {
    public InvalidRateRuleException(String message) {
        super(message);
    }
}