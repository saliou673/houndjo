package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.organization.Organization;

/**
 * Use case for registering and retrieving organizations (schools).
 */
public interface OrganizationUseCase {

    /**
     * Registers a new organization, resolving a collision-free slug before persisting it.
     *
     * @param organization the organization to register, built via {@link Organization#create}
     * @return the persisted organization
     */
    Organization registerSchool(Organization organization);

    /**
     * Returns an organization by its identifier.
     *
     * @param id the organization identifier
     * @return the organization
     */
    Organization getById(Long id);
}
