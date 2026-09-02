package com.houndjo.domain.exceptions;

/**
 * Thrown when an email address fails format validation while constructing an {@code Email} value object.
 */
public class InvalidUserNameException extends FunctionalException {
    public InvalidUserNameException(String value) {
        super("error.user.invalid-email-format", "Invalid email format: " + value, value);
    }
}
