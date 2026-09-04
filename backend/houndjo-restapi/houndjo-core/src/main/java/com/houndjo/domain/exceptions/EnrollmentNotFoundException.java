package com.houndjo.domain.exceptions;

/**
 * Thrown when an enrollment cannot be found by the given identifier within the active
 * organization.
 */
public class EnrollmentNotFoundException extends FunctionalException {
    public EnrollmentNotFoundException(Long id) {
        super("error.enrollment.not-found", "No enrollment found with id " + id + ".", id);
    }
}
