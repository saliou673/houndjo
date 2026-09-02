package com.houndjo.domain.models.auth;

/**
 * Result of a login attempt.
 * Either authentication is complete (JWT issued) or a 2FA challenge must be completed first.
 */
public sealed interface LoginResult {

    /**
     * Authentication is complete — JWT tokens are ready.
     */
    record Complete(JwtToken token) implements LoginResult {}

    /**
     * A 2FA challenge has been created and the user must verify it to receive tokens.
     */
    record TwoFactorRequired(String challengeId, TwoFactorMethodType type) implements LoginResult {}
}
