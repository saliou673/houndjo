package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.AttendancePermissionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link AttendancePermissionEntity}.
 */
@Transactional(readOnly = true)
public interface AttendancePermissionRepository extends JpaRepository<AttendancePermissionEntity, Long> {

    Optional<AttendancePermissionEntity> findByIdAndOrganizationId(Long id, Long organizationId);

    List<AttendancePermissionEntity> findByStudentIdAndOrganizationIdOrderByFromDateDesc(
            Long studentId, Long organizationId);
}
