package com.houndjo.domain.models.academic;

import com.houndjo.domain.enumerations.CourseType;

/**
 * Polymorphic, type-specific tracking configuration for a {@link Course}. Each {@link CourseType}
 * has exactly one variant, so course-type logic never needs a hardcoded {@code switch} on
 * {@code CourseType} outside this hierarchy.
 */
public sealed interface TrackingConfig permits QuranTrackingConfig, QaidaTrackingConfig, BookTrackingConfig {

    CourseType type();
}
