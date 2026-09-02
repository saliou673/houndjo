package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotNull;

/**
 * Request to assign or revoke a role group for a user.
 *
 * @param roleGroupId the identifier of the role group to assign
 */
public record AssignRoleGroupRequest(@NotNull Long roleGroupId) {}
