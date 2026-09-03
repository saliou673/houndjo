package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.organization.OrganizationInvitation;
import com.houndjo.domain.models.query.PagedResult;
import java.util.Optional;

public interface OrganizationInvitationPersistencePort {
    OrganizationInvitation save(OrganizationInvitation invitation);

    Optional<OrganizationInvitation> findByCode(String code);

    Optional<OrganizationInvitation> findByIdAndOrganizationId(Long id, Long organizationId);

    PagedResult<OrganizationInvitation> findPendingByOrganizationId(Long organizationId, int page, int size);

    boolean existsByCode(String code);
}
