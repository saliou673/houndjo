package com.houndjo.domain.enumerations;

/**
 * A user's business role within a specific organization, carried by {@link
 * com.houndjo.domain.models.membership.Membership} — distinct from the platform-wide RBAC
 * role groups.
 */
public enum OrganizationRole {
    /**
     * Founding owner of the school. Full control over the organization.
     */
    SCHOOL_OWNER,
    /**
     * Delegated administrator of the school.
     */
    SCHOOL_ADMIN,
    /**
     * Teacher within the school.
     */
    TEACHER
}
