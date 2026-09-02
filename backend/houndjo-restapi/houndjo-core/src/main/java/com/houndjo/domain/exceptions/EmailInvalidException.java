package com.houndjo.domain.exceptions;

/**
 * Thrown when an email address fails format validation.
 */
public class EmailInvalidException extends FunctionalException {
    public EmailInvalidException(String email) {
        super("error.auth.invalid-email", "Invalid email: " + email, email);
    }
}
