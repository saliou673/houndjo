package com.houndjo.domain.enumerations;

/**
 * Roll-call status of a {@link com.houndjo.domain.models.attendance.Attendance} for a session.
 */
public enum AttendanceStatus {
    PRESENT,
    ABSENT_JUSTIFIED,
    ABSENT_UNJUSTIFIED,
    PERMISSION
}
