package com.houndjo.domain.exceptions;

/**
 * Thrown when a fee schedule cannot be found by the given identifier within the active
 * organization.
 */
public class FeeScheduleNotFoundException extends FunctionalException {
    public FeeScheduleNotFoundException(Long id) {
        super("error.fee-schedule.not-found", "No fee schedule found with id " + id + ".", id);
    }
}
