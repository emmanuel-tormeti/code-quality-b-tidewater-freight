package com.tidewater.freight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ShippingExceptionTest {

    @Test
    void keepsMessageAndCauseWhenWrapping() {
        IllegalStateException cause = new IllegalStateException("db down");

        ShippingException wrapped = new ShippingException("Could not load rates", cause);

        assertEquals("Could not load rates", wrapped.getMessage());
        assertSame(cause, wrapped.getCause());
    }
}