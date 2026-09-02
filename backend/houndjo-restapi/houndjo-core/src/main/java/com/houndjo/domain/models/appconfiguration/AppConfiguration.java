package com.houndjo.domain.models.appconfiguration;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import lombok.Getter;

/**
 * Domain entity representing a configurable reference-data entry (e.g. a supported currency).
 */
@Getter
public class AppConfiguration extends Auditable<Long> {

    /**
     * Functional category this entry belongs to (e.g. CURRENCY, TWO_FACTOR).
     */
    private final AppConfigurationCategory category;
    /**
     * Short unique code within the category (e.g. {@code "EUR"}).
     */
    private String code;
    /**
     * Human-readable label displayed in the UI.
     */
    private String label;
    /**
     * Optional longer description.
     */
    private String description;
    /**
     * Whether this entry is currently enabled.
     */
    private boolean active;

    private AppConfiguration(
            Long id,
            AppConfigurationCategory category,
            String code,
            String label,
            String description,
            boolean active,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.category = category;
        this.code = code;
        this.label = label;
        this.description = description;
        this.active = active;
    }

    public static AppConfiguration create(
            AppConfigurationCategory category, String code, String label, String description) {
        return new AppConfiguration(null, category, code, label, description, true, null, null, null);
    }

    public static AppConfiguration rehydrate(
            Long id,
            AppConfigurationCategory category,
            String code,
            String label,
            String description,
            boolean active,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new AppConfiguration(
                id, category, code, label, description, active, creationDate, lastUpdateDate, lastUpdatedBy);
    }

    public void update(String code, String label, String description, boolean active) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.active = active;
    }
}
