package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.domain.models.appconfiguration.AppConfiguration;
import java.util.Optional;

/**
 * Persistence port for application configuration entries.
 */
public interface AppConfigurationPersistencePort {

    /**
     * Persists or updates a configuration entry.
     *
     * @param appConfiguration the entry to save
     * @return the saved entry
     */
    AppConfiguration save(AppConfiguration appConfiguration);

    /**
     * Finds a configuration entry by its identifier.
     *
     * @param id the entry identifier
     * @return the matching entry, or empty if not found
     */
    Optional<AppConfiguration> findById(Long id);

    /**
     * Checks whether an entry with the given category and code exists.
     *
     * @param category the configuration category
     * @param code     the configuration code
     * @return {@code true} if such an entry exists
     */
    boolean existsByCategoryAndCode(AppConfigurationCategory category, String code);

    /**
     * Checks whether an entry with the given category and code exists, excluding one specific entry.
     *
     * @param category  the configuration category
     * @param code      the configuration code
     * @param excludeId the identifier to exclude from the check
     * @return {@code true} if a conflicting entry exists
     */
    boolean existsByCategoryAndCodeAndIdNot(AppConfigurationCategory category, String code, Long excludeId);

    /**
     * Checks whether an active entry with the given category and code exists.
     *
     * @param category the configuration category
     * @param code     the configuration code
     * @return {@code true} if an active entry with that category and code exists
     */
    boolean existsActiveByCategoryAndCode(AppConfigurationCategory category, String code);

    /**
     * Finds a configuration entry by its category and code.
     *
     * @param category the configuration category
     * @param code     the configuration code
     * @return the matching entry, or empty if not found
     */
    Optional<AppConfiguration> findByCategoryAndCode(AppConfigurationCategory category, String code);

    /**
     * Removes a configuration entry.
     *
     * @param appConfiguration the entry to remove
     */
    void remove(AppConfiguration appConfiguration);
}
