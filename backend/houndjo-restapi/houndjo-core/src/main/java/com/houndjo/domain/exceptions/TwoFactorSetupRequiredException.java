package com.houndjo.domain.exceptions;

/**
 * Thrown when a user attempts to log in but 2FA is globally required
 * and the user has not yet configured 2FA on their account.
 */
public class TwoFactorSetupRequiredException extends AuthFunctionalException {
    public TwoFactorSetupRequiredException() {
        super(
                "error.auth.two-factor-setup-required",
                "Two-factor authentication is required for all users. Please set up 2FA before logging in.");
    }
}
