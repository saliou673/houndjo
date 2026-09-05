package com.houndjo.domain.models.attendance;

import com.houndjo.domain.enumerations.AttendanceStatus;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate representing a student's roll-call status for a single session.
 */
@Getter
public class Attendance extends Auditable<Long> {

    private final Long organizationId;
    private final Long studentId;
    private final Long sessionId;
    private AttendanceStatus status;
    private String reason;

    private Attendance(
            Long id,
            Long organizationId,
            Long studentId,
            Long sessionId,
            AttendanceStatus status,
            String reason,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.status = status;
        this.reason = reason;
    }

    public static Attendance create(
            Long organizationId, Long studentId, Long sessionId, AttendanceStatus status, String reason) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(studentId, "studentId must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Attendance(null, organizationId, studentId, sessionId, status, reason, null, null, null);
    }

    public static Attendance rehydrate(
            Long id,
            Long organizationId,
            Long studentId,
            Long sessionId,
            AttendanceStatus status,
            String reason,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new Attendance(
                id, organizationId, studentId, sessionId, status, reason, creationDate, lastUpdateDate, lastUpdatedBy);
    }

    public void update(AttendanceStatus status, String reason) {
        Objects.requireNonNull(status, "status must not be null");
        this.status = status;
        this.reason = reason;
    }
}
