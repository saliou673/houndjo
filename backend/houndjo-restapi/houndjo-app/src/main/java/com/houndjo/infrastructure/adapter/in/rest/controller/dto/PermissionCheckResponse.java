package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

/**
 * Response indicating whether the caller holds a specific permission.
 *
 * @param hasPermission {@code true} if the permission is granted
 */
public record PermissionCheckResponse(boolean hasPermission) {}
