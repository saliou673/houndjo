package com.houndjo.domain.models.attendance;

import com.houndjo.domain.enumerations.AttendancePermissionStatus;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate representing a planned absence/leave authorization request for a student, covering a
 * date range. Distinct from the platform's RBAC {@code Permission} (authorization codes) — named
 * {@code AttendancePermission} to avoid colliding with it.
 */
@Getter
public class AttendancePermission extends Auditable<Long> {

    private final Long organizationId;
    private final Long studentId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final String reason;
    private AttendancePermissionStatus status;

    private AttendancePermission(
            Long id,
            Long organizationId,
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate,
            String reason,
            AttendancePermissionStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.studentId = studentId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
        this.status = status;
    }

    public static AttendancePermission create(
            Long organizationId, Long studentId, LocalDate fromDate, LocalDate toDate, String reason) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(studentId, "studentId must not be null");
        Objects.requireNonNull(fromDate, "fromDate must not be null");
        Objects.requireNonNull(toDate, "toDate must not be null");
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must not be after toDate");
        }
        return new AttendancePermission(
                null,
                organizationId,
                studentId,
                fromDate,
                toDate,
                reason,
                AttendancePermissionStatus.PENDING,
                null,
                null,
                null);
    }

    public static AttendancePermission rehydrate(
            Long id,
            Long organizationId,
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate,
            String reason,
            AttendancePermissionStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new AttendancePermission(
                id,
                organizationId,
                studentId,
                fromDate,
                toDate,
                reason,
                status,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    /**
     * Returns whether this permission's date range covers the given date (inclusive).
     */
    public boolean covers(LocalDate date) {
        return !date.isBefore(fromDate) && !date.isAfter(toDate);
    }

    public void updateStatus(AttendancePermissionStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        this.status = status;
    }
}
