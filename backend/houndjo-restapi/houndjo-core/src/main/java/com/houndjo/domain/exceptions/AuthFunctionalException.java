package com.houndjo.domain.exceptions;

/**
 * Base functional exception for authentication/authorization errors.
 * <p>
 * Use this to group auth-related functional errors and handle them consistently.
 */
public class AuthFunctionalException extends FunctionalException {
    protected AuthFunctionalException(String code, String message, Object... args) {
        super(code, message, args);
    }
}
