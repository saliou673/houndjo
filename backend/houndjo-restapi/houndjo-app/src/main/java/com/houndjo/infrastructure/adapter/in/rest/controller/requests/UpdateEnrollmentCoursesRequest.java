package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import java.util.Set;

/**
 * Request to add and/or remove courses from an enrollment.
 *
 * @param addCourseIds    courses to add to the enrollment
 * @param removeCourseIds courses to remove from the enrollment
 */
public record UpdateEnrollmentCoursesRequest(
        @Nullable Set<Long> addCourseIds, @Nullable Set<Long> removeCourseIds) {}
