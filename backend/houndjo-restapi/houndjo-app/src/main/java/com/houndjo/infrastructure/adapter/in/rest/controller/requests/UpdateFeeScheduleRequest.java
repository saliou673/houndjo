package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.FeeType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request to update a fee schedule of the active organization.
 *
 * @param type         new fee type
 * @param amount       new amount
 * @param currencyCode optional currency code (ISO 4217); inherited from the organization's
 *                     default currency when blank
 * @param label        new display label
 * @param active       whether the schedule should be active
 */
public record UpdateFeeScheduleRequest(
        @NotNull FeeType type,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @Nullable @Size(max = 10) String currencyCode,
        @NotBlank @Size(max = 150) String label,
        boolean active) {}
