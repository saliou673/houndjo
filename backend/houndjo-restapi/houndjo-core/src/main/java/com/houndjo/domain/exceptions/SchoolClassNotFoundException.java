package com.houndjo.domain.exceptions;

/**
 * Thrown when a school class cannot be found by the given identifier within the active organization.
 */
public class SchoolClassNotFoundException extends FunctionalException {
    public SchoolClassNotFoundException(Long id) {
        super("error.school-class.not-found", "No class found with id " + id + ".", id);
    }
}
