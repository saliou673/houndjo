package com.houndjo.domain.exceptions;

/**
 * Thrown when a membership cannot be found by the given identifier.
 */
public class MembershipNotFoundException extends FunctionalException {
    public MembershipNotFoundException(Long id) {
        super("error.membership.not-found", "No membership found with id " + id + ".", id);
    }
}
