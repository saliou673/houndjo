package com.houndjo.domain.exceptions;

/**
 * Thrown when the token value stored within a refresh token record is invalid.
 */
public class InvalidRefreshTokenTokenException extends FunctionalException {

    public InvalidRefreshTokenTokenException() {
        super("error.auth.refresh-token-invalid-token", "The refresh token is invalid.");
    }
}
