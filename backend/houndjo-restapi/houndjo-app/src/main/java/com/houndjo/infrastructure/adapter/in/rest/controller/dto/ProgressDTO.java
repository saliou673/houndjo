package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Response DTO representing a progress record. Only the portion fields relevant to {@code flow}
 * are populated; the others are {@code null}.
 *
 * @param id           progress record identifier
 * @param studentId    the recorded student identifier
 * @param courseId     the owning course identifier
 * @param sessionId    the session the progress was recorded during
 * @param flow         the tracking flow
 * @param quranPortion populated for the Quran flows ({@code SABAK}/{@code SABQI}/{@code DHOR})
 * @param lessonId     populated for the {@code LESSON} flow
 * @param chapterNo    populated for the {@code CHAPTER} flow
 * @param pageNo       populated for the {@code CHAPTER} flow
 * @param errorCount   number of errors made
 * @param fluency      fluency assessment
 * @param tajweed      optional tajweed assessment
 * @param status       validation status
 * @param note         optional free-text note
 * @param creationDate when the progress record was created
 */
@Schema(name = "Progress")
public record ProgressDTO(
        Long id,
        Long studentId,
        Long courseId,
        Long sessionId,
        ProgressFlow flow,
        QuranPortionDTO quranPortion,
        Long lessonId,
        Integer chapterNo,
        Integer pageNo,
        int errorCount,
        FluencyRating fluency,
        FluencyRating tajweed,
        ProgressStatus status,
        String note,
        Instant creationDate) {

    /**
     * The verse range worked on for a Quran flow.
     */
    @Schema(name = "ProgressQuranPortion")
    public record QuranPortionDTO(int fromSurah, int fromVerse, int toSurah, int toVerse) {}
}
