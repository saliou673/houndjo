package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.FeeScheduleEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link FeeScheduleEntity}.
 */
@Transactional(readOnly = true)
public interface FeeScheduleRepository extends JpaRepository<FeeScheduleEntity, Long> {

    Page<FeeScheduleEntity> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<FeeScheduleEntity> findByIdAndOrganizationId(Long id, Long organizationId);
}
