package com.houndjo.domain.exceptions;

/**
 * Thrown when the provided currency code does not match any active CURRENCY configuration entry.
 */
public class InvalidCurrencyException extends FunctionalException {
    public InvalidCurrencyException(String currencyCode) {
        super("error.billing.invalid-currency", "Invalid currency code: " + currencyCode, currencyCode);
    }
}
