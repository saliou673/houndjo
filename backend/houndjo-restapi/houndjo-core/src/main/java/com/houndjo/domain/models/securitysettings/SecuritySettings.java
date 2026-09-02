package com.houndjo.domain.models.securitysettings;

import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import lombok.Getter;

/**
 * Domain entity representing the security settings — singleton row.
 */
@Getter
public class SecuritySettings extends Auditable<Long> {

    private boolean twoFactorRequired;

    private SecuritySettings(
            Long id, boolean twoFactorRequired, Instant creationDate, Instant lastUpdateDate, String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.twoFactorRequired = twoFactorRequired;
    }

    public static SecuritySettings create(boolean twoFactorRequired) {
        return new SecuritySettings(null, twoFactorRequired, null, null, null);
    }

    public static SecuritySettings rehydrate(
            Long id, boolean twoFactorRequired, Instant creationDate, Instant lastUpdateDate, String lastUpdatedBy) {
        return new SecuritySettings(id, twoFactorRequired, creationDate, lastUpdateDate, lastUpdatedBy);
    }

    public void update(boolean twoFactorRequired) {
        this.twoFactorRequired = twoFactorRequired;
    }
}
