package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.exceptions.OrganizationNotFoundException;
import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.models.organization.OrganizationProfileUpdate;
import com.houndjo.domain.ports.in.OrganizationUseCase;
import com.houndjo.domain.ports.out.persistenceport.MembershipPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.OrganizationPersistencePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link OrganizationUseCase}: school registration and lookup.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationService implements OrganizationUseCase {

    private final OrganizationPersistencePort organizationPersistencePort;
    private final MembershipPersistencePort membershipPersistencePort;
    private final TenantContext tenantContext;

    @Override
    public Organization registerSchool(Organization organization, Long creatorUserId) {
        log.debug("Registering organization: name={}", organization.getName());

        organizationPersistencePort.acquireSlugAllocationLock(organization.getSlug());
        organization.assignSlug(resolveUniqueSlug(organization.getSlug()));
        Organization saved = organizationPersistencePort.save(organization);

        Membership ownerMembership = Membership.create(creatorUserId, saved.getId(), OrganizationRole.SCHOOL_OWNER);
        membershipPersistencePort.save(ownerMembership);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Organization getById(Long id) {
        return organizationPersistencePort.findById(id).orElseThrow(() -> new OrganizationNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> getMyOrganizations(Long userId) {
        List<Long> organizationIds = membershipPersistencePort.findActiveByUserId(userId).stream()
                .map(Membership::getOrganizationId)
                .toList();
        return organizationPersistencePort.findByIds(organizationIds);
    }

    @Override
    public Organization updateProfile(Long id, OrganizationProfileUpdate update) {
        log.debug("Updating organization profile id={}", id);

        Long activeOrganizationId = tenantContext.requireCurrentOrganizationId();
        if (!activeOrganizationId.equals(id)) {
            throw new OrganizationNotFoundException(id);
        }

        Organization organization = getById(id);
        organization.updateProfile(update);
        return organizationPersistencePort.save(organization);
    }

    private String resolveUniqueSlug(String baseSlug) {
        String candidate = baseSlug;
        int suffix = 2;
        while (organizationPersistencePort.existsBySlug(candidate)) {
            candidate = baseSlug + "-" + suffix++;
        }
        return candidate;
    }
}
