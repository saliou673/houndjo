package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.models.organization.OrganizationInvitation;
import com.houndjo.domain.models.query.PagedResult;

public interface OrganizationInvitationUseCase {
    OrganizationInvitation invite(Long organizationId, String email, OrganizationRole role);

    OrganizationInvitation.AcceptanceResult accept(String code, String password);

    PagedResult<OrganizationInvitation> listPending(Long organizationId, int page, int size);

    void revoke(Long organizationId, Long invitationId);
}
