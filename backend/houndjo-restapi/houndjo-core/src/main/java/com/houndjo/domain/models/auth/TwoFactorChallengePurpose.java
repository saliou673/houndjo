package com.houndjo.domain.models.auth;

/**
 * Purpose of a two-factor challenge.
 */
public enum TwoFactorChallengePurpose {
    /**
     * Challenge created during login to complete authentication.
     */
    LOGIN,
    /**
     * Challenge created during 2FA setup to verify the method before enabling it.
     */
    SETUP
}
