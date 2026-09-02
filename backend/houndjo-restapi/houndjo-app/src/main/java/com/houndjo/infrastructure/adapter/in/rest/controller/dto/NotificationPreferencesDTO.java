package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST DTO for notification preferences.
 */
@Schema(name = "NotificationPreferences")
public record NotificationPreferencesDTO(boolean productUpdatesEnabled) {}
