package com.houndjo.domain.exceptions;

/**
 * Thrown when an account activation is attempted on an account that is already active.
 */
public class UserAlreadyActivatedException extends FunctionalException {
    public UserAlreadyActivatedException() {
        super("error.user.already-activated", "This user account is already activated.");
    }
}
