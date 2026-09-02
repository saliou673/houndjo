package com.houndjo.domain.models.user;

/**
 * Lightweight projection of an authenticated user used within the domain layer.
 *
 * @param email       the user's email (JWT subject)
 * @param authorities space-separated permission codes
 */
public record AuthenticatedUser(String email, String authorities) {}
