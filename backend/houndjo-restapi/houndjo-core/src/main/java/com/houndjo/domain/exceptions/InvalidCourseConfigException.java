package com.houndjo.domain.exceptions;

import com.houndjo.domain.enumerations.CourseType;

/**
 * Thrown when a course's type-specific fields are missing or inconsistent for its {@link CourseType}.
 */
public class InvalidCourseConfigException extends FunctionalException {
    public InvalidCourseConfigException(CourseType type, String requiredFields) {
        super(
                "error.course.invalid-config",
                "Course type " + type + " requires: " + requiredFields + ".",
                type,
                requiredFields);
    }
}
