package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.userpreference.UserPreferences;

/**
 * Use case for managing the authenticated user's preferences.
 */
public interface UserPreferenceUseCase {

    /**
     * Returns preferences for the current authenticated user.
     *
     * @return current preferences, or defaults if none have been persisted yet
     */
    UserPreferences getCurrentUserPreferences();

    /**
     * Updates preferences for the current authenticated user.
     *
     * @param preferences validated preference payload
     * @return saved preferences
     */
    UserPreferences updateCurrentUserPreferences(UserPreferences preferences);
}
