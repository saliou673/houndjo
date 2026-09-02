package com.houndjo.domain.exceptions;

/**
 * Thrown when a user cannot be found by the given criteria.
 */
public class UserNotFoundException extends FunctionalException {
    public UserNotFoundException(String email) {
        super("error.user.not-found-by-email", "No user found with email " + email + ".", email);
    }

    public UserNotFoundException(Long id) {
        super("error.user.not-found-by-id", "No user found with id " + id + ".", id);
    }
}
