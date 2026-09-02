package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.securitysettings.SecuritySettings;

/**
 * Use case for managing the singleton security settings.
 */
public interface SecuritySettingsUseCase {

    /**
     * Creates or updates the security settings (upsert semantics).
     *
     * @param twoFactorRequired whether 2FA is globally required for all users
     * @return the saved security settings
     */
    SecuritySettings upsert(boolean twoFactorRequired);

    /**
     * Returns the current security settings.
     * If no settings have been configured yet, returns defaults (twoFactorRequired=false).
     *
     * @return the security settings
     */
    SecuritySettings get();
}
