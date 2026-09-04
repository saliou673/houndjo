package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

/**
 * Response DTO representing an enrollment.
 *
 * @param id          enrollment identifier
 * @param studentId   enrolled student identifier
 * @param studentName the student's full name
 * @param classId     owning class identifier
 * @param className   the class's display name
 * @param courseIds   courses taken within the class
 * @param status      enrollment status
 * @param startDate   when the enrollment started
 * @param endDate     when the enrollment ended, if any
 */
@Schema(name = "Enrollment")
public record EnrollmentDTO(
        Long id,
        Long studentId,
        String studentName,
        Long classId,
        String className,
        Set<Long> courseIds,
        EnrollmentStatus status,
        LocalDate startDate,
        LocalDate endDate) {}
