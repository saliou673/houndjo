package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.domain.enumerations.MembershipStatus;
import com.houndjo.infrastructure.adapter.out.persistence.entity.MembershipEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link MembershipEntity}.
 */
@Transactional(readOnly = true)
public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {

    Page<MembershipEntity> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<MembershipEntity> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<MembershipEntity> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    List<MembershipEntity> findByUserIdAndStatusOrderByCreationDateAsc(Long userId, MembershipStatus status);
}
