package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.FeeType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Response DTO representing a fee schedule.
 *
 * @param id           fee schedule identifier
 * @param type         the fee type
 * @param amount       the fee amount
 * @param currencyCode the currency code (ISO 4217)
 * @param label        display label
 * @param active       whether the schedule is currently active
 */
@Schema(name = "FeeSchedule")
public record FeeScheduleDTO(
        Long id, FeeType type, BigDecimal amount, String currencyCode, String label, boolean active) {}
