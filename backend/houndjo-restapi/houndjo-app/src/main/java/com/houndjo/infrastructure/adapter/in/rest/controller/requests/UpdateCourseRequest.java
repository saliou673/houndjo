package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.QuranMode;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request to update a course of a class. See {@link CreateCourseRequest} for the type-specific
 * field requirements.
 */
public record UpdateCourseRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull CourseType type,
        @Nullable String description,
        @Nullable List<@NotBlank @Size(max = 150) String> qaidaLessons,
        @Nullable QuranMode quranMode,
        @Nullable Integer quranScopeFromJuz,
        @Nullable Integer quranScopeToJuz,
        @Nullable String bookTitle,
        @Nullable @Positive @Max(Short.MAX_VALUE) Integer bookTotalChapters,
        @Nullable @Positive @Max(Short.MAX_VALUE) Integer bookTotalPages) {}
