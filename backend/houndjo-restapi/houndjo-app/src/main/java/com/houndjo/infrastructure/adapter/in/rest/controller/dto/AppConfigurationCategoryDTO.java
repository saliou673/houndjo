package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST DTO representing an application configuration category with its description.
 */
@Schema(name = "AppConfigurationCategory")
public record AppConfigurationCategoryDTO(AppConfigurationCategory value, String description) {
    public static AppConfigurationCategoryDTO from(AppConfigurationCategory category) {
        return new AppConfigurationCategoryDTO(category, category.getDescription());
    }
}
