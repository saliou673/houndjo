package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppConfigurationRequest(
        @NotNull(message = "category is required") AppConfigurationCategory category,

        @NotBlank(message = "code is required") @Size(max = 50, message = "code must not exceed 50 characters")
        String code,

        @NotBlank(message = "label is required") String label,

        @Nullable String description) {}
