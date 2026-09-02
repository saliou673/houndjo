package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * Request to update an existing role group.
 *
 * @param name            new display name
 * @param description     new description
 * @param permissionCodes new set of permission codes
 */
public record UpdateRoleGroupRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotEmpty Set<String> permissionCodes) {}
