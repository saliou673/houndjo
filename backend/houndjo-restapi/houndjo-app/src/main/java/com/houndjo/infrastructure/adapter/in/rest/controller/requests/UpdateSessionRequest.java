package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request to update a session of a course.
 *
 * @param sessionDate   the session's new date
 * @param startTime     optional new start time
 * @param endTime       optional new end time
 * @param teacherUserId optional new assigned teacher's user identifier
 */
public record UpdateSessionRequest(
        @NotNull LocalDate sessionDate,
        @Nullable LocalTime startTime,
        @Nullable LocalTime endTime,
        @Nullable Long teacherUserId) {}
