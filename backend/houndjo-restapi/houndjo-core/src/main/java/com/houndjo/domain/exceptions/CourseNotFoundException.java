package com.houndjo.domain.exceptions;

/**
 * Thrown when a course cannot be found by the given identifier within the active organization's class.
 */
public class CourseNotFoundException extends FunctionalException {
    public CourseNotFoundException(Long id) {
        super("error.course.not-found", "No course found with id " + id + ".", id);
    }
}
