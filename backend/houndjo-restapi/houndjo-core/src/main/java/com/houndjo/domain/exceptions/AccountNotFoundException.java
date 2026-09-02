package com.houndjo.domain.exceptions;

/**
 * Thrown when no account is found for the given credentials or identifier.
 */
public class AccountNotFoundException extends AuthFunctionalException {

    public AccountNotFoundException(String email) {
        super("error.auth.account-not-found", "No account found for " + email + ".", email);
    }
}
