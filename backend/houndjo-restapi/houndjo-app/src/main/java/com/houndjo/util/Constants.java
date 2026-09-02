package com.houndjo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Application-wide constants for validation patterns and REST defaults.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    /**
     * Regex pattern enforcing strong passwords (upper, lower, digit, special character, min 8 chars).
     */
    public static final String PASSWORD_REGEX_PATTERN =
            "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$";

    /**
     * Default page size used in REST pagination query parameters.
     */
    public static final String DEFAULT_REST_PAGE_SIZE = "20";
    /**
     * Default page number used in REST pagination query parameters.
     */
    public static final String DEFAULT_REST_PAGE_NUMBER = "0";
}
