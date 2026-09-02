package com.houndjo.domain.exceptions;

/**
 * Thrown when a login attempt is made against a disabled account.
 */
public class AccountDisabledException extends AuthFunctionalException {

    public AccountDisabledException() {
        super("error.auth.account-disabled", "Your account is disabled.");
    }
}
