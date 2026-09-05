package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Response DTO representing a student's roll-call status for a session.
 *
 * @param id          attendance record identifier
 * @param studentId   the student identifier
 * @param studentName the student's full name
 * @param sessionId   the session identifier
 * @param sessionDate the session's date
 * @param status      roll-call status
 * @param reason      optional free-text reason
 */
@Schema(name = "Attendance")
public record AttendanceDTO(
        Long id,
        Long studentId,
        String studentName,
        Long sessionId,
        LocalDate sessionDate,
        AttendanceStatus status,
        String reason) {}
