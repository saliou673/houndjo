package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.auth.JwtToken;
import com.houndjo.domain.models.auth.LoginResult;

/**
 * Use case for user authentication and token management.
 */
public interface AuthenticationUseCase {
    /**
     * Authenticates a user. Returns a complete JWT if 2FA is not enabled,
     * or a TwoFactorRequired result containing a challenge ID if 2FA is enabled.
     *
     * @param email      The user's email.
     * @param password   The user's password.
     * @param rememberMe Whether to generate a long-lived refresh token.
     * @return A LoginResult (either complete with JWT or requiring 2FA).
     */
    LoginResult login(String email, String password, boolean rememberMe);

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param refreshToken The refresh token to use for generating a new access token.
     * @return A JwtToken containing the new access token and a new refresh token.
     */
    JwtToken refreshToken(String refreshToken);

    /**
     * Logs out a user by invalidating the current access token session.
     *
     * @param accessToken The current JWT access token.
     */
    void logout(String accessToken);
}
