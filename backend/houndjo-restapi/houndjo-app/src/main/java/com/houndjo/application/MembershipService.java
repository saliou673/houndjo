package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.exceptions.MembershipNotFoundException;
import com.houndjo.domain.exceptions.OrganizationNotFoundException;
import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.MembershipUseCase;
import com.houndjo.domain.ports.out.persistenceport.MembershipPersistencePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link MembershipUseCase}: organization membership management.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MembershipService implements MembershipUseCase {

    private final MembershipPersistencePort membershipPersistencePort;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Membership> findByOrganizationId(Long organizationId, int page, int size) {
        requireActiveOrganization(organizationId);
        return membershipPersistencePort.findByOrganizationId(organizationId, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Membership> findActiveByUserId(Long userId) {
        return membershipPersistencePort.findActiveByUserId(userId);
    }

    @Override
    public Membership changeRole(Long organizationId, Long id, OrganizationRole newRole) {
        requireActiveOrganization(organizationId);
        log.debug("Changing role of membership id={} in organization={} to {}", id, organizationId, newRole);
        Membership membership = getByIdOrThrow(organizationId, id);
        membership.changeRole(newRole);
        return membershipPersistencePort.save(membership);
    }

    @Override
    public void revoke(Long organizationId, Long id) {
        requireActiveOrganization(organizationId);
        log.debug("Revoking membership id={} in organization={}", id, organizationId);
        Membership membership = getByIdOrThrow(organizationId, id);
        membership.revoke();
        membershipPersistencePort.save(membership);
    }

    private Membership getByIdOrThrow(Long organizationId, Long id) {
        return membershipPersistencePort
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new MembershipNotFoundException(id));
    }

    private void requireActiveOrganization(Long organizationId) {
        if (!tenantContext.requireCurrentOrganizationId().equals(organizationId)) {
            throw new OrganizationNotFoundException(organizationId);
        }
    }
}
