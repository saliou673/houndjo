package com.houndjo.domain.exceptions;

/**
 * Thrown when a course has no pace configured yet.
 */
public class CoursePaceNotFoundException extends FunctionalException {
    public CoursePaceNotFoundException(Long courseId) {
        super("error.course-pace.not-found", "No pace configured for course with id " + courseId + ".", courseId);
    }
}
