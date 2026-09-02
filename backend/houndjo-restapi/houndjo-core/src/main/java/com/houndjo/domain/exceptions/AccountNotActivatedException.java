package com.houndjo.domain.exceptions;

/**
 * Thrown when a login attempt is made against an account that has not been activated yet.
 */
public class AccountNotActivatedException extends AuthFunctionalException {

    public AccountNotActivatedException(String email) {
        super("error.auth.account-not-activated", "The account for " + email + " is not activated.", email);
    }
}
