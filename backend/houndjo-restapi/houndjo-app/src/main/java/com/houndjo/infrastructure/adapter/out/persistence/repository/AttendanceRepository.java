package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.AttendanceEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link AttendanceEntity}.
 */
@Transactional(readOnly = true)
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    Optional<AttendanceEntity> findByStudentIdAndSessionIdAndOrganizationId(
            Long studentId, Long sessionId, Long organizationId);

    List<AttendanceEntity> findBySessionIdAndOrganizationId(Long sessionId, Long organizationId);

    // Theta join against SessionEntity: attendance does not carry its own session date, so the
    // session's date is used to bound the range. fromDate/toDate must never be null: the caller
    // substitutes wide-open sentinel bounds for "no constraint".
    @Query("SELECT a FROM AttendanceEntity a, SessionEntity s WHERE a.sessionId = s.id "
            + "AND a.studentId = :studentId AND a.organizationId = :organizationId "
            + "AND s.sessionDate >= :fromDate AND s.sessionDate <= :toDate "
            + "ORDER BY s.sessionDate ASC")
    List<AttendanceEntity> findByStudentAndSessionDateRange(
            @Param("studentId") Long studentId,
            @Param("organizationId") Long organizationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
