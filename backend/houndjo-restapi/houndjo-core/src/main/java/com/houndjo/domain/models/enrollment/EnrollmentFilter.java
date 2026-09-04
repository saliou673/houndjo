package com.houndjo.domain.models.enrollment;

import com.houndjo.domain.enumerations.EnrollmentStatus;

/**
 * Filter for listing enrollments within the active organization. Null fields mean "no
 * constraint".
 *
 * @param classId   optional owning class identifier
 * @param studentId optional enrolled student identifier
 * @param status    optional enrollment status
 */
public record EnrollmentFilter(Long classId, Long studentId, EnrollmentStatus status) {}
