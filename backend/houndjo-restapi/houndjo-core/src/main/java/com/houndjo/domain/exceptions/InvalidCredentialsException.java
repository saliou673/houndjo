package com.houndjo.domain.exceptions;

/**
 * Thrown when the provided email/password combination is incorrect.
 */
public class InvalidCredentialsException extends AuthFunctionalException {

    public InvalidCredentialsException() {
        super("error.auth.invalid-credentials", "Incorrect email or password.");
    }
}
