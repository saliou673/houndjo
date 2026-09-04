package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.domain.enumerations.EnrollmentStatus;
import com.houndjo.infrastructure.adapter.out.persistence.entity.EnrollmentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link EnrollmentEntity}.
 */
@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    Optional<EnrollmentEntity> findByIdAndOrganizationId(Long id, Long organizationId);

    List<EnrollmentEntity> findByStudentIdAndOrganizationId(Long studentId, Long organizationId);

    boolean existsByStudentIdAndClassIdAndOrganizationIdAndStatus(
            Long studentId, Long classId, Long organizationId, EnrollmentStatus status);

    @Query("SELECT e FROM EnrollmentEntity e WHERE e.organizationId = :organizationId "
            + "AND (:classId IS NULL OR e.classId = :classId) "
            + "AND (:studentId IS NULL OR e.studentId = :studentId) "
            + "AND (:status IS NULL OR e.status = :status)")
    Page<EnrollmentEntity> search(
            @Param("organizationId") Long organizationId,
            @Param("classId") Long classId,
            @Param("studentId") Long studentId,
            @Param("status") EnrollmentStatus status,
            Pageable pageable);
}
