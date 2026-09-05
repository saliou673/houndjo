package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.domain.ports.out.persistenceport.AttendancePersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.AttendanceMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.AttendanceRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link AttendancePersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class AttendancePersistenceAdapter implements AttendancePersistencePort {

    // Wide-open sentinel bounds standing in for "no constraint" on the corresponding side of the
    // date range, well within PostgreSQL's DATE range.
    private static final LocalDate MIN_DATE = LocalDate.of(1, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    public List<Attendance> findBySessionIdAndOrganizationId(Long sessionId, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> attendanceMapper.toDomain(
                        attendanceRepository.findBySessionIdAndOrganizationId(sessionId, organizationId)),
                "Error fetching session attendance");
    }

    @Override
    public List<Attendance> findByStudentIdAndOrganizationIdAndSessionDateBetween(
            Long studentId, Long organizationId, LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate == null ? MIN_DATE : fromDate;
        LocalDate to = toDate == null ? MAX_DATE : toDate;
        return AdapterPersistenceUtils.executeDbOperation(
                () -> attendanceMapper.toDomain(
                        attendanceRepository.findByStudentAndSessionDateRange(studentId, organizationId, from, to)),
                "Error fetching student attendance history");
    }

    @Override
    @Transactional
    public List<Attendance> saveAll(List<Attendance> attendances) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> attendanceMapper.toDomain(attendanceRepository.saveAll(attendanceMapper.toEntity(attendances))),
                "Error saving attendance records");
    }
}
