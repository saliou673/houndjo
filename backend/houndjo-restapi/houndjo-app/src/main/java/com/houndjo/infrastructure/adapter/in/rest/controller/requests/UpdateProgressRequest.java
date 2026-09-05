package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request to correct an existing progress record's portion and assessment. The student, course,
 * session and flow are immutable; see {@link RecordProgressRequest} for the type-specific
 * portion field requirements.
 */
public record UpdateProgressRequest(
        @Nullable Integer fromSurah,
        @Nullable Integer fromVerse,
        @Nullable Integer toSurah,
        @Nullable Integer toVerse,
        @Nullable Long lessonId,
        @Nullable Integer chapterNo,
        @Nullable Integer pageNo,
        @PositiveOrZero int errorCount,
        @NotNull FluencyRating fluency,
        @Nullable FluencyRating tajweed,
        @NotNull ProgressStatus status,
        @Nullable @Size(max = 2000) String note) {}
