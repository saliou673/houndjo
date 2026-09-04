package com.houndjo.domain.exceptions;

/**
 * Thrown when a session cannot be found by the given identifier within its course and the active
 * organization.
 */
public class SessionNotFoundException extends FunctionalException {
    public SessionNotFoundException(Long id) {
        super("error.session.not-found", "No session found with id " + id + ".", id);
    }
}
