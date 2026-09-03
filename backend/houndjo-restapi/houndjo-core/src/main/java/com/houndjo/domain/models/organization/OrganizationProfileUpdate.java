package com.houndjo.domain.models.organization;

/**
 * Editable profile fields of an {@link Organization}, applied via {@link
 * Organization#updateProfile}.
 *
 * @param name                new display name
 * @param contactEmail        new contact email
 * @param phoneNumber         new phone number
 * @param address             new address
 * @param defaultCurrencyCode new default currency code
 * @param defaultLanguageKey  new default language key
 * @param timezone            new timezone
 */
public record OrganizationProfileUpdate(
        String name,
        String contactEmail,
        String phoneNumber,
        String address,
        String defaultCurrencyCode,
        String defaultLanguageKey,
        String timezone) {}
