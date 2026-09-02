package com.houndjo.domain.exceptions;

/**
 * Thrown when the authentication process fails for a generic reason.
 */
public class AuthenticationFailedException extends AuthFunctionalException {

    public AuthenticationFailedException(String reason) {
        super("error.auth.authentication-failed", "Authentication failed: " + reason, reason);
    }
}
