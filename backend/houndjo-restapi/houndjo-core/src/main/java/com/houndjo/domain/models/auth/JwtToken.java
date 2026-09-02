package com.houndjo.domain.models.auth;

/**
 * Represents a JWT Authentication token.
 */
public record JwtToken(String accessToken, String refreshToken) {}
