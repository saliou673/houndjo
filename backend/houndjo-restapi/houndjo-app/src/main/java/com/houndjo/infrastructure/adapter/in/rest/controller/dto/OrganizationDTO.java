package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.OrganizationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "Organization")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
/** Response DTO representing an organization (school). */
public class OrganizationDTO extends AuditableDTO {

    private Long id;
    private String name;
    private String slug;
    private String contactEmail;
    private String phoneNumber;
    private String address;
    private String defaultCurrencyCode;
    private String defaultLanguageKey;
    private String timezone;
    private OrganizationStatus status;

    public OrganizationDTO(
            Long id,
            String name,
            String slug,
            String contactEmail,
            String phoneNumber,
            String address,
            String defaultCurrencyCode,
            String defaultLanguageKey,
            String timezone,
            OrganizationStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(creationDate, lastUpdateDate, lastUpdatedBy);
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.contactEmail = contactEmail;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.defaultCurrencyCode = defaultCurrencyCode;
        this.defaultLanguageKey = defaultLanguageKey;
        this.timezone = timezone;
        this.status = status;
    }
}
