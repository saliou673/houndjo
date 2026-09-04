package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.PaceUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Response DTO representing a course's pace configuration. {@code sabak}/{@code sabqi}/
 * {@code dhor}/{@code dhorCycleDays} are populated only for {@code QURAN} courses.
 *
 * @param courseId         the course identifier
 * @param unit             base pace unit
 * @param amountPerSession base target amount per session, in {@code unit}
 * @param sessionsPerWeek  weekly session cadence
 * @param sabak            Sabak (new lesson) flow pace
 * @param sabqi            Sabqi (recent review) flow pace
 * @param dhor             Dhor (long-term revision) flow pace
 * @param dhorCycleDays    length in days of the Dhor revision cycle
 */
@Schema(name = "Pace")
public record PaceDTO(
        Long courseId,
        PaceUnit unit,
        BigDecimal amountPerSession,
        int sessionsPerWeek,
        PaceFlowDTO sabak,
        PaceFlowDTO sabqi,
        PaceFlowDTO dhor,
        Integer dhorCycleDays) {}
