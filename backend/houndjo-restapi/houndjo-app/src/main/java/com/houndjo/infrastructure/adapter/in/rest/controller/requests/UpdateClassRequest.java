package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to update a class of the active organization.
 *
 * @param name         new display name
 * @param description  new description
 * @param displayOrder new ordering hint among the organization's classes
 */
public record UpdateClassRequest(
        @NotBlank @Size(max = 120) String name, @Nullable String description, @Nullable Integer displayOrder) {}
