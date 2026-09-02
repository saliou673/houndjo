package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.models.userpreference.TextSizePreference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * REST DTO for display preferences.
 */
@Schema(name = "DisplayPreferences")
public record DisplayPreferencesDTO(@NotNull TextSizePreference textSize, boolean reduceMotion) {}
