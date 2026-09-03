package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.organization.Organization;
import java.util.Optional;

/**
 * Persistence port for organizations.
 */
public interface OrganizationPersistencePort {

    /**
     * Finds an organization by its identifier.
     *
     * @param id the organization identifier
     * @return the matching organization, or empty if not found
     */
    Optional<Organization> findById(Long id);

    /**
     * Checks whether an organization with the given slug already exists.
     *
     * @param slug the slug to check
     * @return {@code true} if an organization with that slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Persists or updates an organization.
     *
     * @param organization the organization to save
     * @return the saved organization
     */
    Organization save(Organization organization);
}
