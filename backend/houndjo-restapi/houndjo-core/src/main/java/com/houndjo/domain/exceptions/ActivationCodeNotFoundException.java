package com.houndjo.domain.exceptions;

/**
 * Thrown when no account matches the given activation code.
 */
public class ActivationCodeNotFoundException extends FunctionalException {

    public ActivationCodeNotFoundException(String activationCode) {
        super(
                "error.auth.activation-code-not-found",
                "No account found for activation code " + activationCode + ".",
                activationCode);
    }
}
