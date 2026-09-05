package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.attendance.Attendance;
import java.time.LocalDate;
import java.util.List;

/**
 * Persistence port for session roll-call attendance.
 */
public interface AttendancePersistencePort {

    /**
     * Returns the attendance records of a session within an organization.
     *
     * @param sessionId      the session identifier
     * @param organizationId the owning organization identifier
     * @return the matching attendance records
     */
    List<Attendance> findBySessionIdAndOrganizationId(Long sessionId, Long organizationId);

    /**
     * Returns a student's attendance records within an organization whose session falls between
     * two dates (inclusive), ordered by session date.
     *
     * @param studentId      the student identifier
     * @param organizationId the owning organization identifier
     * @param fromDate       earliest session date, inclusive
     * @param toDate         latest session date, inclusive
     * @return the matching attendance records
     */
    List<Attendance> findByStudentIdAndOrganizationIdAndSessionDateBetween(
            Long studentId, Long organizationId, LocalDate fromDate, LocalDate toDate);

    /**
     * Persists a batch of attendance records (new or updated).
     *
     * @param attendances the attendance records to save
     * @return the saved attendance records, in the same order
     */
    List<Attendance> saveAll(List<Attendance> attendances);
}
