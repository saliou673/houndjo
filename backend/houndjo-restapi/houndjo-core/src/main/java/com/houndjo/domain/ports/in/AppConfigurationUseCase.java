package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.domain.models.appconfiguration.AppConfiguration;
import java.util.Optional;

/**
 * Use case for managing application configuration entries (reference data).
 */
public interface AppConfigurationUseCase {

    /**
     * Creates a new configuration entry.
     *
     * @param category    functional category
     * @param code        short unique code within the category
     * @param label       display label
     * @param description optional description
     * @return the created entry
     */
    AppConfiguration create(AppConfigurationCategory category, String code, String label, String description);

    /**
     * Updates the configuration entry with the given identifier.
     *
     * @param id          the entry identifier
     * @param code        new code
     * @param label       new label
     * @param description new description
     * @param active      new active flag
     * @return the updated entry
     */
    AppConfiguration update(Long id, String code, String label, String description, boolean active);

    /**
     * Updates a configuration entry identified by its category and current code.
     *
     * @param category    the category
     * @param code        the current code
     * @param newCode     the new code
     * @param label       new label
     * @param description new description
     * @param active      new active flag
     * @return the updated entry
     */
    AppConfiguration updateByCategoryAndCode(
            AppConfigurationCategory category,
            String code,
            String newCode,
            String label,
            String description,
            boolean active);

    /**
     * Deletes the configuration entry with the given identifier.
     *
     * @param id the entry identifier
     */
    void delete(Long id);

    /**
     * Returns the configuration entry with the given identifier.
     *
     * @param id the entry identifier
     * @return the entry
     */
    AppConfiguration getById(Long id);

    /**
     * Returns the configuration entry for the given category and code if it exists.
     *
     * @param category the category
     * @param code     the code
     * @return an Optional containing the entry, or empty if not found
     */
    Optional<AppConfiguration> getByCategoryAndCode(AppConfigurationCategory category, String code);
}
