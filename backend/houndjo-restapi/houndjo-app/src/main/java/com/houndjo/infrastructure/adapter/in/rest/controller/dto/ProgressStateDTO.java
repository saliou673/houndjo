package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO representing a student's computed progress state for a {@code QURAN} course.
 * {@code alerts} mirrors {@code stalePortions} as a flat, top-level list for a dashboard badge.
 *
 * @param sabak         the last validated Sabak portion, or {@code null} if none recorded yet
 * @param sabqi         the last validated Sabqi portion, or {@code null} if none recorded yet
 * @param coveredJuz    every juz Dhor-revised at least once, ascending
 * @param stalePortions juz containing overdue verses, including memorized verses never Dhor-revised
 * @param alerts        same content as {@code stalePortions}
 */
@Schema(name = "ProgressState")
public record ProgressStateDTO(
        FlowSnapshotDTO sabak,
        FlowSnapshotDTO sabqi,
        List<Integer> coveredJuz,
        List<StaleDhorPortionDTO> stalePortions,
        List<StaleDhorPortionDTO> alerts) {

    /**
     * The last validated portion recorded for a flow (Sabak or Sabqi).
     */
    @Schema(name = "ProgressFlowSnapshot")
    public record FlowSnapshotDTO(int fromSurah, int fromVerse, int toSurah, int toVerse, Instant date) {}

    /**
     * A juz containing overdue verses. lastReviewedDate is null for a never-reviewed verse.
     */
    @Schema(name = "StaleDhorPortion")
    public record StaleDhorPortionDTO(int juz, Instant lastReviewedDate, long daysSince) {}
}
