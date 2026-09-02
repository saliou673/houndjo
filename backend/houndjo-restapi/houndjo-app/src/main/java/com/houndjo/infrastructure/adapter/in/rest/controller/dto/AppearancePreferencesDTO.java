package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.models.userpreference.FontPreference;
import com.houndjo.domain.models.userpreference.ThemePreference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * REST DTO for appearance preferences.
 */
@Schema(name = "AppearancePreferences")
public record AppearancePreferencesDTO(
        @NotNull ThemePreference theme, @NotNull FontPreference font) {}
