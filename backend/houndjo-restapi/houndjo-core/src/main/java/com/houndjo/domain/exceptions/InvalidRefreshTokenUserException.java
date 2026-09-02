package com.houndjo.domain.exceptions;

/**
 * Thrown when the user associated with a refresh token does not match the expected user.
 */
public class InvalidRefreshTokenUserException extends FunctionalException {

    public InvalidRefreshTokenUserException() {
        super("error.auth.refresh-token-invalid-user", "The refresh token user is invalid.");
    }
}
