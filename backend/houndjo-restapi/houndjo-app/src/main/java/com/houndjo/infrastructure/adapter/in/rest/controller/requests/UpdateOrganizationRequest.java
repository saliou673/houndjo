package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to update an organization's profile.
 *
 * @param name                new display name
 * @param contactEmail        new contact email
 * @param phoneNumber         optional new phone number
 * @param address             optional new address
 * @param defaultCurrencyCode optional new default currency code
 * @param defaultLanguageKey  optional new default language key
 * @param timezone            optional new timezone
 */
public record UpdateOrganizationRequest(
        @NotBlank String name,
        @NotBlank @Email String contactEmail,
        @Nullable String phoneNumber,
        @Nullable String address,
        @Nullable String defaultCurrencyCode,
        @Nullable String defaultLanguageKey,
        @Nullable String timezone) {}
