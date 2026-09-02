package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO representing the security settings.
 */
@Schema(name = "SecuritySettings")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class SecuritySettingsDTO extends AuditableDTO {

    private Long id;
    private boolean twoFactorRequired;

    public SecuritySettingsDTO(
            Long id, boolean twoFactorRequired, Instant creationDate, Instant lastUpdateDate, String lastUpdatedBy) {
        super(creationDate, lastUpdateDate, lastUpdatedBy);
        this.id = id;
        this.twoFactorRequired = twoFactorRequired;
    }
}
