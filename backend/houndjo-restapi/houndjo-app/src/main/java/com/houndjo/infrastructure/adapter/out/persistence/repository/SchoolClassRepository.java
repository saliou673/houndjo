package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.SchoolClassEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link SchoolClassEntity}.
 */
@Transactional(readOnly = true)
public interface SchoolClassRepository extends JpaRepository<SchoolClassEntity, Long> {

    Page<SchoolClassEntity> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<SchoolClassEntity> findByIdAndOrganizationId(Long id, Long organizationId);
}
