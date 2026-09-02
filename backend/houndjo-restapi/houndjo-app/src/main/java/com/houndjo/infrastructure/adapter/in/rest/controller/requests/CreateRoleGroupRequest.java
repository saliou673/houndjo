package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * Request to create a new role group.
 *
 * @param name            unique display name
 * @param description     purpose description
 * @param permissionCodes set of permission codes to assign
 */
public record CreateRoleGroupRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotEmpty Set<String> permissionCodes) {}
