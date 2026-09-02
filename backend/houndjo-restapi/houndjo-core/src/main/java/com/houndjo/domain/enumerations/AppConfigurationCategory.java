package com.houndjo.domain.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Categories used to group application configuration entries.
 */
@Getter
@RequiredArgsConstructor
public enum AppConfigurationCategory {
    /**
     * Supported currency codes (e.g. EUR, USD).
     */
    CURRENCY("Supported currency codes (e.g. EUR, USD)"),
    /**
     * Two-factor authentication provider settings.
     */
    TWO_FACTOR("Two-factor authentication provider settings"),
    /**
     * Supported payment modes (e.g. STRIPE, PAYPAL).
     */
    PAYMENT_MODE("Supported payment modes (e.g. STRIPE, PAYPAL)");

    private final String description;
}
