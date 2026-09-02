package com.houndjo.domain.exceptions;

/**
 * Thrown when attempting to create a user with an email that is already registered.
 */
public class UserAlreadyExistsException extends FunctionalException {
    public UserAlreadyExistsException(String email) {
        super("error.user.already-exists", "The email " + email + " is already in use.", email);
    }
}
