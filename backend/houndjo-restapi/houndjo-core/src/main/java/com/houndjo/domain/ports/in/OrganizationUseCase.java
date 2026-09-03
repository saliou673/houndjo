package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.models.organization.OrganizationProfileUpdate;
import java.util.List;

/**
 * Use case for registering and retrieving organizations (schools).
 */
public interface OrganizationUseCase {

    /**
     * Registers a new organization, resolving a collision-free slug before persisting it, and
     * grants the creator a {@code SCHOOL_OWNER} membership in the same transaction.
     *
     * @param organization  the organization to register, built via {@link Organization#create}
     * @param creatorUserId the identifier of the user registering the school
     * @return the persisted organization
     */
    Organization registerSchool(Organization organization, Long creatorUserId);

    /**
     * Returns an organization by its identifier.
     *
     * @param id the organization identifier
     * @return the organization
     */
    Organization getById(Long id);

    /**
     * Returns the organizations the given user has an active membership in.
     *
     * @param userId the user identifier
     * @return the user's organizations
     */
    List<Organization> getMyOrganizations(Long userId);

    /**
     * Updates an organization's profile. Only callable for the currently active organization
     * (see {@code com.houndjo.application.tenant.TenantContext}) — attempting to update any
     * other organization id behaves as not-found.
     *
     * @param id     the organization identifier
     * @param update the profile fields to apply
     * @return the updated organization
     */
    Organization updateProfile(Long id, OrganizationProfileUpdate update);
}
