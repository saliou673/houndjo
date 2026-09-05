package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.domain.models.attendance.AttendanceEntry;
import com.houndjo.domain.models.attendance.StudentAttendanceHistory;
import java.time.LocalDate;
import java.util.List;

/**
 * Use case for recording and reviewing roll-call attendance within the active organization.
 */
public interface AttendanceUseCase {

    /**
     * Records roll-call for a session in one shot. Idempotent: an entry for a student already
     * having an attendance record for the session updates it instead of duplicating it. The last
     * entry wins for duplicate student ids in one request. Statuses are explicitly chosen by
     * the teacher; approving a leave request does not create or rewrite roll-call records.
     *
     * @param sessionId the session identifier
     * @param entries   the roll-call entries
     * @return the resulting attendance records
     */
    List<Attendance> recordBulk(Long sessionId, List<AttendanceEntry> entries);

    /**
     * Returns the attendance records of a session within the active organization.
     *
     * @param sessionId the session identifier
     * @return the matching attendance records
     */
    List<Attendance> findBySession(Long sessionId);

    /**
     * Returns a student's attendance history between two dates (inclusive), together with the
     * resulting absence rate.
     *
     * @param studentId the student identifier
     * @param fromDate  earliest session date, inclusive; {@code null} means no lower bound
     * @param toDate    latest session date, inclusive; {@code null} means no upper bound
     * @return the student's attendance history
     */
    StudentAttendanceHistory getStudentHistory(Long studentId, LocalDate fromDate, LocalDate toDate);
}
