package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.OrganizationRole;
import jakarta.validation.constraints.NotNull;

/**
 * Request to change an existing membership's organization role.
 *
 * @param role the new organization role
 */
public record ChangeMembershipRoleRequest(@NotNull OrganizationRole role) {}
