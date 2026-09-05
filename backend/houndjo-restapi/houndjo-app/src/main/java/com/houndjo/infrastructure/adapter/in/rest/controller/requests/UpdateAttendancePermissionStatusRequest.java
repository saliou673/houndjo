package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.AttendancePermissionStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request to update a student leave/absence authorization's approval status.
 *
 * @param status the new status
 */
public record UpdateAttendancePermissionStatusRequest(
        @NotNull AttendancePermissionStatus status) {}
