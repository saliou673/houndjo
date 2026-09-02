package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO representing a permission.
 *
 * @param code        unique permission code (e.g. {@code "user:read"})
 * @param description human-readable description of the permission
 */
@Schema(name = "Permission")
public record PermissionDTO(String code, String description) {}
