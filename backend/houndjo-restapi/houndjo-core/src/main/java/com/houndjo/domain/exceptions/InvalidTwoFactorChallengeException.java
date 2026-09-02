package com.houndjo.domain.exceptions;

/**
 * Thrown when a 2FA challenge is not found, has expired, or the provided code is wrong.
 */
public class InvalidTwoFactorChallengeException extends FunctionalException {

    private InvalidTwoFactorChallengeException(String code, String message) {
        super(code, message);
    }

    public static InvalidTwoFactorChallengeException noPendingSetup() {
        return new InvalidTwoFactorChallengeException(
                "error.two-factor.no-pending-setup", "No pending two-factor setup challenge found.");
    }

    public static InvalidTwoFactorChallengeException setupExpired() {
        return new InvalidTwoFactorChallengeException(
                "error.two-factor.setup-expired",
                "The two-factor setup challenge has expired. Please initiate setup again.");
    }

    public static InvalidTwoFactorChallengeException invalidCode() {
        return new InvalidTwoFactorChallengeException(
                "error.two-factor.invalid-code", "Invalid two-factor authentication code.");
    }

    public static InvalidTwoFactorChallengeException notFoundOrExpired() {
        return new InvalidTwoFactorChallengeException(
                "error.two-factor.not-found-or-expired", "Invalid or expired two-factor challenge.");
    }

    public static InvalidTwoFactorChallengeException invalidPurpose() {
        return new InvalidTwoFactorChallengeException(
                "error.two-factor.invalid-purpose", "Invalid two-factor challenge.");
    }

    public static InvalidTwoFactorChallengeException expired() {
        return new InvalidTwoFactorChallengeException(
                "error.two-factor.challenge-expired", "The two-factor challenge has expired. Please log in again.");
    }
}
