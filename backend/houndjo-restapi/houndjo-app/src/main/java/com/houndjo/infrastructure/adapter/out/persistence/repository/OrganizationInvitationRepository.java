package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.domain.enumerations.InvitationStatus;
import com.houndjo.infrastructure.adapter.out.persistence.entity.OrganizationInvitationEntity;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitationEntity, Long> {
    Optional<OrganizationInvitationEntity> findByInvitationCode(String code);

    Optional<OrganizationInvitationEntity> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<OrganizationInvitationEntity> findByOrganizationIdAndStatus(
            Long organizationId, InvitationStatus status, Pageable pageable);

    boolean existsByInvitationCode(String code);
}
