package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.FeeType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request to create a fee schedule in the active organization.
 *
 * @param type         the fee type
 * @param amount       the fee amount
 * @param currencyCode optional currency code (ISO 4217); inherited from the organization's
 *                     default currency when blank
 * @param label        display label
 */
public record CreateFeeScheduleRequest(
        @NotNull FeeType type,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @Nullable @Size(max = 10) String currencyCode,
        @NotBlank @Size(max = 150) String label) {}
