package com.houndjo.domain.exceptions;

/**
 * Thrown when a student already has an {@code ACTIVE} enrollment in the target class.
 */
public class DuplicateActiveEnrollmentException extends FunctionalException {
    public DuplicateActiveEnrollmentException(Long studentId, Long classId) {
        super(
                "error.enrollment.duplicate-active",
                "Student " + studentId + " already has an active enrollment in class " + classId + ".",
                studentId,
                classId);
    }
}
