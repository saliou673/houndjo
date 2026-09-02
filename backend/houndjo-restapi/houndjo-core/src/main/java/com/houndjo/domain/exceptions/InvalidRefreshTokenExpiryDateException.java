package com.houndjo.domain.exceptions;

/**
 * Thrown when the expiry date of a refresh token is missing or in the past.
 */
public class InvalidRefreshTokenExpiryDateException extends FunctionalException {

    public InvalidRefreshTokenExpiryDateException() {
        super("error.auth.refresh-token-invalid-expiry", "The refresh token expiry date is invalid.");
    }
}
