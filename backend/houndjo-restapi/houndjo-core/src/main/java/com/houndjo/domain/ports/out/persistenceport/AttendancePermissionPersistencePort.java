package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.attendance.AttendancePermission;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port for student leave/absence authorizations.
 */
public interface AttendancePermissionPersistencePort {

    /**
     * Finds a leave authorization by its identifier within an organization.
     *
     * @param id             the authorization identifier
     * @param organizationId the owning organization identifier
     * @return the matching authorization, or empty if not found
     */
    Optional<AttendancePermission> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Returns a student's leave authorizations within an organization, most recent first.
     *
     * @param studentId      the student identifier
     * @param organizationId the owning organization identifier
     * @return the matching authorizations
     */
    List<AttendancePermission> findByStudentIdAndOrganizationId(Long studentId, Long organizationId);

    /**
     * Persists or updates a leave authorization.
     *
     * @param attendancePermission the authorization to save
     * @return the saved authorization
     */
    AttendancePermission save(AttendancePermission attendancePermission);
}
