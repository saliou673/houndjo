package com.houndjo.domain.models.attendance;

import com.houndjo.domain.enumerations.AttendanceStatus;

/**
 * A single roll-call entry submitted as part of a bulk attendance recording for a session.
 *
 * @param studentId the student identifier
 * @param status    the roll-call status
 * @param reason    optional free-text reason (e.g. for a justified absence)
 */
public record AttendanceEntry(Long studentId, AttendanceStatus status, String reason) {}
