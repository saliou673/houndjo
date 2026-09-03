package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
        @NotBlank String name,
        @NotBlank @Email String contactEmail,
        @Nullable String phoneNumber,
        @Nullable String address,
        @Nullable String defaultCurrencyCode,
        @Nullable String defaultLanguageKey) {}
