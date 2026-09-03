package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link OrganizationEntity}.
 */
@Transactional(readOnly = true)
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    boolean existsBySlug(String slug);
}
