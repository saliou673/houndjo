package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

/**
 * Request to enroll a student in a class and its courses.
 *
 * @param studentId the student identifier
 * @param classId   the class identifier
 * @param courseIds courses taken within the class
 */
public record CreateEnrollmentRequest(
        @NotNull Long studentId,
        @NotNull Long classId,
        @Nullable Set<Long> courseIds) {}
