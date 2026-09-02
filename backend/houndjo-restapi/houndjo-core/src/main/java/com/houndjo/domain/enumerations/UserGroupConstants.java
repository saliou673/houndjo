package com.houndjo.domain.enumerations;

import lombok.NoArgsConstructor;

/**
 * Predefined role-group name constants.
 * These match the names seeded in the database and must not be renamed without a migration.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class UserGroupConstants {

    /**
     * System administrator with full access.
     */
    public static final String SYS_ADMIN = "Sysadmin";
    /**
     * Regular administrator.
     */
    public static final String ADMIN = "Admin";
    /**
     * Standard authenticated user.
     */
    public static final String USER = "User";
    /**
     * Internal service or staff account.
     */
    public static final String INTERNAL = "Internal";
    /**
     * External institution or partner account.
     */
    public static final String EXTERNAL_INSTITUTION = "External";
    /**
     * Unauthenticated / public access group.
     */
    public static final String ANONYMOUS = "Anonymous";
}
