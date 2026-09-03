package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.query.PagedResult;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port for organization memberships.
 */
public interface MembershipPersistencePort {

    /**
     * Returns the memberships of an organization, paginated.
     *
     * @param organizationId the organization identifier
     * @param page           zero-based page index
     * @param size           maximum items per page
     * @return paginated memberships
     */
    PagedResult<Membership> findByOrganizationId(Long organizationId, int page, int size);

    /**
     * Finds a membership by its identifier within an organization.
     *
     * @param id             the membership identifier
     * @param organizationId the owning organization identifier
     * @return the matching membership, or empty if not found
     */
    Optional<Membership> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Finds the membership linking a user to an organization, regardless of status.
     *
     * @param userId         the user identifier
     * @param organizationId the organization identifier
     * @return the matching membership, or empty if none
     */
    Optional<Membership> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    /**
     * Returns the active memberships of a user, across all organizations.
     *
     * @param userId the user identifier
     * @return the user's active memberships
     */
    List<Membership> findActiveByUserId(Long userId);

    /**
     * Persists or updates a membership.
     *
     * @param membership the membership to save
     * @return the saved membership
     */
    Membership save(Membership membership);
}
