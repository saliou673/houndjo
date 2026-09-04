package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request to create a single session for a course.
 *
 * @param sessionDate   the session's date
 * @param startTime     optional start time
 * @param endTime       optional end time
 * @param teacherUserId optional assigned teacher's user identifier
 */
public record CreateSessionRequest(
        @NotNull LocalDate sessionDate,
        @Nullable LocalTime startTime,
        @Nullable LocalTime endTime,
        @Nullable Long teacherUserId) {}
