package com.houndjo.domain.exceptions;

/**
 * Thrown when a refresh token is not found or is otherwise invalid.
 */
public class InvalidRefreshTokenException extends FunctionalException {
    public InvalidRefreshTokenException() {
        super("error.auth.invalid-refresh-token", "Invalid refresh token.");
    }
}
