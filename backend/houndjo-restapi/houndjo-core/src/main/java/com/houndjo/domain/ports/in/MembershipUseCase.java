package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.query.PagedResult;
import java.util.List;

/**
 * Use case for managing organization memberships.
 */
public interface MembershipUseCase {

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
     * Returns the active memberships of a user, across all organizations.
     *
     * @param userId the user identifier
     * @return the user's active memberships
     */
    List<Membership> findActiveByUserId(Long userId);

    /**
     * Changes the organization role of an existing membership.
     *
     * @param organizationId the organization identifier from the request path
     * @param id             the membership identifier
     * @param newRole        the new organization role
     * @return the updated membership
     */
    Membership changeRole(Long organizationId, Long id, OrganizationRole newRole);

    /**
     * Revokes a membership.
     *
     * @param organizationId the organization identifier from the request path
     * @param id             the membership identifier
     */
    void revoke(Long organizationId, Long id);
}
