package com.houndjo.domain.exceptions;

/**
 * Thrown when a login attempt is made against an expired account.
 */
public class AccountExpiredException extends AuthFunctionalException {

    public AccountExpiredException() {
        super("error.auth.account-expired", "Your account has expired.");
    }
}
