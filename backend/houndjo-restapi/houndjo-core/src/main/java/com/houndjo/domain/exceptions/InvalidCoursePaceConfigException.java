package com.houndjo.domain.exceptions;

/**
 * Thrown when a pace configuration is inconsistent with its course's type (e.g. a {@code QURAN}
 * course missing one of the Sabak/Sabqi/Dhor flows).
 */
public class InvalidCoursePaceConfigException extends FunctionalException {
    public InvalidCoursePaceConfigException(String requirement) {
        super("error.course-pace.invalid-config", "Invalid pace configuration: " + requirement + ".", requirement);
    }
}
