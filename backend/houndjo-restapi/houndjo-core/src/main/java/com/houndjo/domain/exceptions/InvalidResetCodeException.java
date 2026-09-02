package com.houndjo.domain.exceptions;

/**
 * Thrown when the provided password-reset or invitation code is invalid or has expired.
 */
public class InvalidResetCodeException extends FunctionalException {

    public InvalidResetCodeException() {
        super("error.auth.reset-code-invalid", "The reset code is invalid or has expired.");
    }

    private InvalidResetCodeException(String code, String message) {
        super(code, message);
    }

    public static InvalidResetCodeException forInvitation() {
        return new InvalidResetCodeException(
                "error.auth.invitation-code-invalid", "The invitation code is invalid or has expired.");
    }
}
