package com.houndjo.domain.exceptions;

/**
 * Thrown when the user's credentials (e.g. password) have expired.
 */
public class CredentialsExpiredException extends AuthFunctionalException {

    public CredentialsExpiredException() {
        super("error.auth.credentials-expired", "Your credentials have expired.");
    }
}
