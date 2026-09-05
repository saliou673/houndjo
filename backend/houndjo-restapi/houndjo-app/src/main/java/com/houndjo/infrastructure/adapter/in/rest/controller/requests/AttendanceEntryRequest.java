package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.AttendanceStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A single roll-call entry within a {@link BulkAttendanceRequest}.
 *
 * @param studentId the student identifier
 * @param status    the roll-call status
 * @param reason    optional free-text reason
 */
public record AttendanceEntryRequest(
        @NotNull Long studentId,
        @NotNull AttendanceStatus status,
        @Nullable @Size(max = 255) String reason) {}
