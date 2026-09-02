package com.houndjo.domain.exceptions;

/**
 * Thrown when a login attempt is made against a locked account.
 */
public class AccountLockedException extends AuthFunctionalException {

    public AccountLockedException() {
        super("error.auth.account-locked", "Your account is locked.");
    }
}
