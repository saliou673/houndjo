package com.houndjo.domain.exceptions;

import com.houndjo.domain.enumerations.AppConfigurationCategory;

/**
 * Thrown when a configuration entry cannot be found.
 */
public class AppConfigurationNotFoundException extends FunctionalException {

    public AppConfigurationNotFoundException(Long id) {
        super("error.app-configuration.not-found-by-id", "Reference data not found with id " + id + ".", id);
    }

    public AppConfigurationNotFoundException(AppConfigurationCategory category, String code) {
        super(
                "error.app-configuration.not-found-by-category-code",
                "Reference data not found for category " + category + " and code " + code + ".",
                category,
                code);
    }
}
