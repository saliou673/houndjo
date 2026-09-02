package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "AppConfiguration")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
/** Response DTO representing an application configuration (reference data) entry. */
public class AppConfigurationDTO extends AuditableDTO {

    /**
     * Unique identifier of the configuration entry.
     */
    private Long id;
    /**
     * Functional category of the entry.
     */
    private AppConfigurationCategory category;
    /**
     * Short unique code within the category.
     */
    private String code;
    /**
     * Display label.
     */
    private String label;
    /**
     * Optional description.
     */
    private String description;
    /**
     * Whether the entry is currently active.
     */
    private boolean active;

    public AppConfigurationDTO(
            Long id,
            AppConfigurationCategory category,
            String code,
            String label,
            String description,
            boolean active,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(creationDate, lastUpdateDate, lastUpdatedBy);
        this.id = id;
        this.category = category;
        this.code = code;
        this.label = label;
        this.description = description;
        this.active = active;
    }
}
