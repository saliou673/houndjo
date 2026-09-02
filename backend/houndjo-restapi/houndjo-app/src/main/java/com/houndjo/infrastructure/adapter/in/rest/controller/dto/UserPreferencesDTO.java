package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * REST DTO for user preferences.
 */
@Schema(name = "UserPreferences")
public record UserPreferencesDTO(
        @NotNull @Valid AppearancePreferencesDTO appearance,
        @NotNull @Valid NotificationPreferencesDTO notifications,
        @NotNull @Valid DisplayPreferencesDTO display) {}
