package com.houndjo.application;

import com.houndjo.domain.exceptions.OrganizationNotFoundException;
import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.ports.in.OrganizationUseCase;
import com.houndjo.domain.ports.out.persistenceport.OrganizationPersistencePort;
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

    @Override
    public Organization registerSchool(Organization organization) {
        log.debug("Registering organization: name={}", organization.getName());

        organization.assignSlug(resolveUniqueSlug(organization.getSlug()));
        return organizationPersistencePort.save(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public Organization getById(Long id) {
        return organizationPersistencePort.findById(id).orElseThrow(() -> new OrganizationNotFoundException(id));
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
