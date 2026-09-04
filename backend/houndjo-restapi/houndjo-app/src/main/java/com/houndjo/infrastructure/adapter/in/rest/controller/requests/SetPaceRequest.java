package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.PaceUnit;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request to set (create or replace) a course's pace configuration. {@code sabak}/{@code sabqi}/
 * {@code dhor}/{@code dhorCycleDays} are required for {@code QURAN} courses, ignored otherwise.
 *
 * @param unit             base pace unit
 * @param amountPerSession base target amount per session, in {@code unit}
 * @param sessionsPerWeek  weekly session cadence
 * @param sabak            Sabak (new lesson) flow pace
 * @param sabqi            Sabqi (recent review) flow pace
 * @param dhor             Dhor (long-term revision) flow pace
 * @param dhorCycleDays    length in days of the Dhor revision cycle
 */
public record SetPaceRequest(
        @NotNull PaceUnit unit,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amountPerSession,
        @Positive @Max(7) int sessionsPerWeek,
        @Nullable @Valid FlowRequest sabak,
        @Nullable @Valid FlowRequest sabqi,
        @Nullable @Valid FlowRequest dhor,
        @Nullable @Positive Integer dhorCycleDays) {

    /**
     * @param unit   the unit the amount is expressed in
     * @param amount the target amount per session, in {@code unit}
     */
    public record FlowRequest(
            @NotNull PaceUnit unit,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {}
}
