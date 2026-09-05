package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.attendance.AttendancePermission;
import com.houndjo.domain.ports.out.persistenceport.AttendancePermissionPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.AttendancePermissionMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.AttendancePermissionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link AttendancePermissionPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class AttendancePermissionPersistenceAdapter implements AttendancePermissionPersistencePort {

    private final AttendancePermissionRepository attendancePermissionRepository;
    private final AttendancePermissionMapper attendancePermissionMapper;

    @Override
    public Optional<AttendancePermission> findByIdAndOrganizationId(Long id, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> attendancePermissionRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .map(attendancePermissionMapper::toDomain),
                "Error fetching attendance permission by id");
    }

    @Override
    @Transactional
    public AttendancePermission save(AttendancePermission attendancePermission) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> attendancePermissionMapper.toDomain(
                        attendancePermissionRepository.save(attendancePermissionMapper.toEntity(attendancePermission))),
                "Error saving attendance permission");
    }
}
