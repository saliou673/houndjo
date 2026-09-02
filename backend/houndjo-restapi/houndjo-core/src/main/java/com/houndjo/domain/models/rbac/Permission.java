package com.houndjo.domain.models.rbac;

/**
 * Immutable value object representing a fine-grained permission.
 *
 * @param code        unique identifier used in authority checks (e.g. {@code "user:read"})
 * @param description human-readable explanation of what the permission grants
 */
public record Permission(String code, String description) {}
