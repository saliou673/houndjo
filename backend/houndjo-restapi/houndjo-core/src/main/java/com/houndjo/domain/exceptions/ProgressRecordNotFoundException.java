package com.houndjo.domain.exceptions;

/**
 * Thrown when a progress record cannot be found by the given identifier within the active
 * organization.
 */
public class ProgressRecordNotFoundException extends FunctionalException {
    public ProgressRecordNotFoundException(Long id) {
        super("error.progress.not-found", "No progress record found with id " + id + ".", id);
    }
}
