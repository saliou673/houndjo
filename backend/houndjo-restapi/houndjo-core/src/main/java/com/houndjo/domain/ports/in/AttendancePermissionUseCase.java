package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.AttendancePermissionStatus;
import com.houndjo.domain.models.attendance.AttendancePermission;
import java.time.LocalDate;

/**
 * Use case for managing student planned absence/leave authorizations within the active
 * organization.
 */
public interface AttendancePermissionUseCase {

    /**
     * Requests a new leave authorization for a student over a date range.
     *
     * @param studentId the student identifier
     * @param fromDate  first day of the leave, inclusive
     * @param toDate    last day of the leave, inclusive
     * @param reason    optional free-text reason
     * @return the created authorization, in {@code PENDING} status
     */
    AttendancePermission create(Long studentId, LocalDate fromDate, LocalDate toDate, String reason);

    /**
     * Returns a leave authorization by its identifier within the active organization.
     *
     * @param id the authorization identifier
     * @return the matching authorization
     */
    AttendancePermission getById(Long id);

    /**
     * Updates a leave authorization's approval status.
     *
     * @param id     the authorization identifier
     * @param status the new status
     * @return the updated authorization
     */
    AttendancePermission updateStatus(Long id, AttendancePermissionStatus status);
}
