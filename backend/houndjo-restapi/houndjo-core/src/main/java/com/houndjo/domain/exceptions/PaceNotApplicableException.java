package com.houndjo.domain.exceptions;

/**
 * Thrown when a Quran-flow-specific pace operation (e.g. computing the next portion) is
 * attempted on a course that isn't of type {@code QURAN}.
 */
public class PaceNotApplicableException extends FunctionalException {
    public PaceNotApplicableException(Long courseId) {
        super(
                "error.course-pace.not-applicable",
                "Course " + courseId + " is not a QURAN course; Sabak/Sabqi/Dhor portions don't apply.",
                courseId);
    }
}
