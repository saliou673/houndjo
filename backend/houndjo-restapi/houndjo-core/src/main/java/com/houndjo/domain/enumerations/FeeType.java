package com.houndjo.domain.enumerations;

/**
 * The kind of fee a {@link com.houndjo.domain.models.billing.FeeSchedule} represents.
 */
public enum FeeType {
    /**
     * One-shot registration/enrollment fee.
     */
    REGISTRATION,
    /**
     * Recurring monthly tuition fee.
     */
    TUITION_MONTHLY,
    /**
     * Recurring annual tuition fee.
     */
    TUITION_ANNUAL
}
