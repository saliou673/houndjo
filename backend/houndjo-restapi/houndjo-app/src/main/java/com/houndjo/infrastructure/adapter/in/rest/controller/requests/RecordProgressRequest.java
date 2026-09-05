package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request to record a progress entry for a (student, session) pair on a given flow. Which
 * portion fields are required depends on {@code flow} ({@code fromSurah}/{@code fromVerse}/
 * {@code toSurah}/{@code toVerse} for the Quran flows, {@code lessonId} for {@code LESSON},
 * {@code chapterNo}/{@code pageNo} for {@code CHAPTER}), enforced by the application layer.
 */
public record RecordProgressRequest(
        @NotNull Long studentId,
        @NotNull Long courseId,
        @NotNull Long sessionId,
        @NotNull ProgressFlow flow,
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
