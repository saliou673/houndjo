package com.houndjo.domain.exceptions;

/**
 * Thrown when an organization cannot be found by the given identifier.
 */
public class OrganizationNotFoundException extends FunctionalException {
    public OrganizationNotFoundException(Long id) {
        super("error.organization.not-found", "No organization found with id " + id + ".", id);
    }
}
