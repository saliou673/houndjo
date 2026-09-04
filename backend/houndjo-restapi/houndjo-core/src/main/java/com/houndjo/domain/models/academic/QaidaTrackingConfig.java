package com.houndjo.domain.models.academic;

import com.houndjo.domain.enumerations.CourseType;

/**
 * Tracking config for a {@code QAIDA} course. Lesson-level configuration (ordered list of Qaida
 * lessons) is deferred to a later ticket — QAIDA courses need no type-specific fields yet.
 */
public record QaidaTrackingConfig() implements TrackingConfig {

    @Override
    public CourseType type() {
        return CourseType.QAIDA;
    }
}
