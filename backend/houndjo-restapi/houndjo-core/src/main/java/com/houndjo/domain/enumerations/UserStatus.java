package com.houndjo.domain.enumerations;

/**
 * Lifecycle status of a user account.
 */
public enum UserStatus {
    /**
     * Account created but email not yet confirmed.
     */
    NOT_ACTIVATED,
    /**
     * Account active and usable.
     */
    ACTIVATED,
    /**
     * Account suspended by the user or an admin.
     */
    DEACTIVATED,
    /**
     * Account temporarily locked (e.g. too many failed logins).
     */
    LOCKED,
    /**
     * Account permanently banned.
     */
    BANNED
}
