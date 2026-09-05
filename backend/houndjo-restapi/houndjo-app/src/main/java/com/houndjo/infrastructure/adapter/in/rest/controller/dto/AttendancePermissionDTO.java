package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.AttendancePermissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Response DTO representing a student leave/absence authorization.
 *
 * @param id        authorization identifier
 * @param studentId the student identifier
 * @param fromDate  first day of the leave, inclusive
 * @param toDate    last day of the leave, inclusive
 * @param reason    optional free-text reason
 * @param status    approval status
 */
@Schema(name = "AttendancePermission")
public record AttendancePermissionDTO(
        Long id,
        Long studentId,
        LocalDate fromDate,
        LocalDate toDate,
        String reason,
        AttendancePermissionStatus status) {}
