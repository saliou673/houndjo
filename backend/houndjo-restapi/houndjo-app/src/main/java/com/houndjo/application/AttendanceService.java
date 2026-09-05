package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.AttendanceStatus;
import com.houndjo.domain.exceptions.SessionNotFoundException;
import com.houndjo.domain.exceptions.StudentNotFoundException;
import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.domain.models.attendance.AttendanceEntry;
import com.houndjo.domain.models.attendance.StudentAttendanceHistory;
import com.houndjo.domain.ports.in.AttendanceUseCase;
import com.houndjo.domain.ports.out.persistenceport.AttendancePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.SessionPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link AttendanceUseCase}: session roll-call recording and
 * student attendance history, scoped to the active organization.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceService implements AttendanceUseCase {

    private final AttendancePersistencePort attendancePersistencePort;
    private final SessionPersistencePort sessionPersistencePort;
    private final StudentPersistencePort studentPersistencePort;
    private final TenantContext tenantContext;

    @Override
    public List<Attendance> recordBulk(Long sessionId, List<AttendanceEntry> entries) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireSession(sessionId, organizationId);
        log.debug(
                "Recording bulk attendance: organizationId={} sessionId={} entries={}",
                organizationId,
                sessionId,
                entries.size());
        Map<Long, Attendance> existingByStudentId =
                attendancePersistencePort.findBySessionIdAndOrganizationId(sessionId, organizationId).stream()
                        .collect(Collectors.toMap(Attendance::getStudentId, Function.identity()));
        List<Attendance> toSave = new ArrayList<>();
        for (AttendanceEntry entry : entries) {
            requireStudent(entry.studentId(), organizationId);
            Attendance attendance = existingByStudentId.get(entry.studentId());
            if (attendance != null) {
                attendance.update(entry.status(), entry.reason());
            } else {
                attendance =
                        Attendance.create(organizationId, entry.studentId(), sessionId, entry.status(), entry.reason());
            }
            toSave.add(attendance);
        }
        return attendancePersistencePort.saveAll(toSave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findBySession(Long sessionId) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireSession(sessionId, organizationId);
        return attendancePersistencePort.findBySessionIdAndOrganizationId(sessionId, organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentAttendanceHistory getStudentHistory(Long studentId, LocalDate fromDate, LocalDate toDate) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireStudent(studentId, organizationId);
        List<Attendance> entries = attendancePersistencePort.findByStudentIdAndOrganizationIdAndSessionDateBetween(
                studentId, organizationId, fromDate, toDate);
        long absentCount = entries.stream()
                .filter(a -> a.getStatus() != AttendanceStatus.PRESENT)
                .count();
        double absenceRate = entries.isEmpty() ? 0.0 : (double) absentCount / entries.size();
        return new StudentAttendanceHistory(entries, absenceRate);
    }

    private void requireSession(Long sessionId, Long organizationId) {
        sessionPersistencePort
                .findByIdAndOrganizationId(sessionId, organizationId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    private void requireStudent(Long studentId, Long organizationId) {
        studentPersistencePort
                .findByIdAndOrganizationId(studentId, organizationId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
    }
}
