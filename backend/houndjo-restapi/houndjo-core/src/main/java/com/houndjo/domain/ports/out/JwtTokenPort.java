package com.houndjo.domain.ports.out;

import com.houndjo.domain.models.user.AuthenticatedUser;
import java.time.Instant;

/**
 * Port for JWT token generation operations.
 */
public interface JwtTokenPort {

    String ACTIVE_ORGANIZATION_CLAIM = "orgId";

    /**
     * Authenticates a user and returns domain representation.
     *
     * @param email    User email
     * @param password User password
     * @return AuthenticatedUser with email and authorities
     */
    AuthenticatedUser authenticate(String email, String password);

    /**
     * Generates an access token.
     *
     * @param email       User email (subject)
     * @param authorities User authorities (space-separated roles)
     * @param expiryDate           Token expiration date
     * @param activeOrganizationId the user's default active organization, or null when none exists
     * @return The generated JWT access token
     */
    String generateAccessToken(String email, String authorities, Instant expiryDate, Long activeOrganizationId);

    /**
     * Calculates token validity based on remember me option.
     *
     * @param rememberMe If true, returns extended validity, otherwise short-lived
     * @return The token expiry instant
     */
    Instant calculateTokenValidity(boolean rememberMe);
}
