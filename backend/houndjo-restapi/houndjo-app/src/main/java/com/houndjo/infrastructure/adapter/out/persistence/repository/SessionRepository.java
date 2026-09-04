package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.SessionEntity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link SessionEntity}.
 */
@Transactional(readOnly = true)
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    Optional<SessionEntity> findByIdAndCourseIdAndOrganizationId(Long id, Long courseId, Long organizationId);

    // fromDate/toDate must never be null: the caller substitutes wide-open sentinel bounds for
    // "no constraint" (a null bind parameter used only in an IS NULL check defeats Postgres's
    // parameter type inference).
    @Query("SELECT s FROM SessionEntity s WHERE s.courseId = :courseId AND s.organizationId = :organizationId "
            + "AND s.sessionDate >= :fromDate AND s.sessionDate <= :toDate")
    Page<SessionEntity> search(
            @Param("courseId") Long courseId,
            @Param("organizationId") Long organizationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);
}
