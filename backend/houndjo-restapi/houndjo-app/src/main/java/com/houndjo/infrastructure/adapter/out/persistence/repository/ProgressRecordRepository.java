package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.infrastructure.adapter.out.persistence.entity.ProgressRecordEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link ProgressRecordEntity}.
 */
@Transactional(readOnly = true)
public interface ProgressRecordRepository extends JpaRepository<ProgressRecordEntity, Long> {

    Optional<ProgressRecordEntity> findByIdAndOrganizationId(Long id, Long organizationId);

    // fromDate/toDate must never be null: the caller substitutes wide-open sentinel bounds for
    // "no constraint" (a null bind parameter used only in an IS NULL check defeats Postgres's
    // parameter type inference).
    @Query("SELECT p FROM ProgressRecordEntity p WHERE p.organizationId = :organizationId "
            + "AND (:studentId IS NULL OR p.studentId = :studentId) "
            + "AND (:courseId IS NULL OR p.courseId = :courseId) "
            + "AND (:flow IS NULL OR p.flow = :flow) "
            + "AND p.creationDate >= :fromDate AND p.creationDate <= :toDate")
    Page<ProgressRecordEntity> search(
            @Param("organizationId") Long organizationId,
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("flow") ProgressFlow flow,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);
}
