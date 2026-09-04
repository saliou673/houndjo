package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO representing a course session.
 *
 * @param id            session identifier
 * @param courseId      owning course identifier
 * @param teacherUserId optional assigned teacher's user identifier
 * @param teacherName   optional assigned teacher's full name
 * @param sessionDate   the session's date
 * @param startTime     optional start time
 * @param endTime       optional end time
 * @param status        session status
 */
@Schema(name = "Session")
public record SessionDTO(
        Long id,
        Long courseId,
        Long teacherUserId,
        String teacherName,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        SessionStatus status) {}
