package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.StudentEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link StudentEntity}.
 */
@Transactional(readOnly = true)
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    List<StudentEntity> findByIdInAndOrganizationId(Collection<Long> ids, Long organizationId);

    Page<StudentEntity> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<StudentEntity> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("SELECT s FROM StudentEntity s WHERE s.organizationId = :organizationId "
            + "AND (LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<StudentEntity> searchByOrganizationId(
            @Param("organizationId") Long organizationId, @Param("search") String search, Pageable pageable);
}
