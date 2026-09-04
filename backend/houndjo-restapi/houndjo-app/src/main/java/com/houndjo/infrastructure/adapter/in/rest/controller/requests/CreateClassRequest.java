package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to create a class in the active organization.
 *
 * @param name         display name
 * @param description  optional description
 * @param displayOrder optional ordering hint among the organization's classes
 */
public record CreateClassRequest(
        @NotBlank @Size(max = 120) String name,
        @Nullable String description,
        @Nullable Integer displayOrder) {}
