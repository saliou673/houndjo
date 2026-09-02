package com.houndjo.domain.exceptions;

/**
 * Thrown when a payment gateway returns a failure result.
 */
public class PaymentProcessingException extends FunctionalException {
    public PaymentProcessingException(String reason) {
        super("error.billing.payment-processing-failed", "Payment processing failed: " + reason, reason);
    }
}
