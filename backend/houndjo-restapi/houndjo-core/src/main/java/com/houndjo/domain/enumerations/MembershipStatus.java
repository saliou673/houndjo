package com.houndjo.domain.enumerations;

/**
 * Lifecycle status of a {@link com.houndjo.domain.models.membership.Membership}.
 */
public enum MembershipStatus {
    /**
     * Membership active and usable.
     */
    ACTIVE,
    /**
     * Membership created from a pending invitation, not yet accepted.
     */
    INVITED,
    /**
     * Membership revoked; the user no longer has this organization role.
     */
    REVOKED
}
