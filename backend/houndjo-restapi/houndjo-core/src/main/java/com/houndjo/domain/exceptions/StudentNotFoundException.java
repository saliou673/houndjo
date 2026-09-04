package com.houndjo.domain.exceptions;

/**
 * Thrown when a student cannot be found by the given identifier within the active organization.
 */
public class StudentNotFoundException extends FunctionalException {
    public StudentNotFoundException(Long id) {
        super("error.student.not-found", "No student found with id " + id + ".", id);
    }
}
