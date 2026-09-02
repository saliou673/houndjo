package com.houndjo.domain.exceptions;

/**
 * Thrown when the provided payment mode is not a configured active PAYMENT_MODE entry.
 */
public class InvalidPaymentModeException extends FunctionalException {
    public InvalidPaymentModeException(String paymentMode) {
        super("error.billing.invalid-payment-mode", "Invalid payment mode: " + paymentMode, paymentMode);
    }
}
