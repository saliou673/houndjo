package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to register a new organization (school).
 *
 * @param name                the school's display name
 * @param contactEmail        the school's contact email address
 * @param phoneNumber         optional contact phone number
 * @param address             optional physical address
 * @param defaultCurrencyCode optional default currency code (ISO 4217), defaults to GNF
 * @param defaultLanguageKey  optional default language key, defaults to fr
 */
public record RegisterSchoolRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 255) String contactEmail,
        @Nullable @Size(max = 20) String phoneNumber,
        @Nullable @Size(max = 255) String address,
        @Nullable @Size(max = 10) String defaultCurrencyCode,
        @Nullable @Size(max = 5) String defaultLanguageKey) {}
