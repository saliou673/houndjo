package com.houndjo.domain.exceptions;

/**
 * Thrown when one or more course identifiers do not belong to the target class.
 */
public class CourseNotInClassException extends FunctionalException {
    public CourseNotInClassException(Long classId) {
        super(
                "error.enrollment.course-not-in-class",
                "One or more courses do not belong to class " + classId + ".",
                classId);
    }
}
