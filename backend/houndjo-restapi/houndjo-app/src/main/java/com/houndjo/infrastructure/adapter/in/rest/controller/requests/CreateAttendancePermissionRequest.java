package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request to create a student leave/absence authorization.
 *
 * @param studentId the student identifier
 * @param fromDate  first day of the leave, inclusive
 * @param toDate    last day of the leave, inclusive
 * @param reason    optional free-text reason
 */
public record CreateAttendancePermissionRequest(
        @NotNull Long studentId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @Nullable @Size(max = 255) String reason) {}
