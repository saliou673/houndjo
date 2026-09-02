package com.houndjo.domain.exceptions;

/**
 * Thrown when the current password provided during a change-password
 * or account-recovery flow is incorrect.
 */
public class InvalidCurrentPasswordException extends FunctionalException {

    public InvalidCurrentPasswordException() {
        super("error.auth.invalid-current-password", "The current password is incorrect.");
    }

    private InvalidCurrentPasswordException(String code, String message) {
        super(code, message);
    }

    public static InvalidCurrentPasswordException forAccountRecovery() {
        return new InvalidCurrentPasswordException(
                "error.auth.recovery-invalid-credentials", "Invalid credentials for account recovery.");
    }
}
