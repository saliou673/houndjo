package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.AttendancePermissionStatus;
import com.houndjo.domain.exceptions.AttendancePermissionNotFoundException;
import com.houndjo.domain.exceptions.StudentNotFoundException;
import com.houndjo.domain.models.attendance.AttendancePermission;
import com.houndjo.domain.ports.in.AttendancePermissionUseCase;
import com.houndjo.domain.ports.out.persistenceport.AttendancePermissionPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link AttendancePermissionUseCase}: student planned
 * absence/leave authorizations, scoped to the active organization.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AttendancePermissionService implements AttendancePermissionUseCase {

    private final AttendancePermissionPersistencePort attendancePermissionPersistencePort;
    private final StudentPersistencePort studentPersistencePort;
    private final TenantContext tenantContext;

    @Override
    public AttendancePermission create(Long studentId, LocalDate fromDate, LocalDate toDate, String reason) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireStudent(studentId, organizationId);
        log.debug(
                "Creating attendance permission: organizationId={} studentId={} fromDate={} toDate={}",
                organizationId,
                studentId,
                fromDate,
                toDate);
        AttendancePermission attendancePermission =
                AttendancePermission.create(organizationId, studentId, fromDate, toDate, reason);
        return attendancePermissionPersistencePort.save(attendancePermission);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendancePermission getById(Long id) {
        return getByIdOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendancePermission> findByStudent(Long studentId) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireStudent(studentId, organizationId);
        return attendancePermissionPersistencePort.findByStudentIdAndOrganizationId(studentId, organizationId);
    }

    @Override
    public AttendancePermission updateStatus(Long id, AttendancePermissionStatus status) {
        log.debug("Updating attendance permission id={} status={}", id, status);
        AttendancePermission attendancePermission = getByIdOrThrow(id);
        attendancePermission.updateStatus(status);
        return attendancePermissionPersistencePort.save(attendancePermission);
    }

    private AttendancePermission getByIdOrThrow(Long id) {
        return attendancePermissionPersistencePort
                .findByIdAndOrganizationId(id, tenantContext.requireCurrentOrganizationId())
                .orElseThrow(() -> new AttendancePermissionNotFoundException(id));
    }

    private void requireStudent(Long studentId, Long organizationId) {
        studentPersistencePort
                .findByIdAndOrganizationId(studentId, organizationId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
    }
}
