package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.QuranMode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Response DTO representing a course. Only the fields relevant to {@code type} are populated;
 * the others are {@code null}.
 *
 * @param id                 course identifier
 * @param classId            the owning class identifier
 * @param name               display name
 * @param type                course typology
 * @param description        optional description
 * @param quranMode          populated for {@code QURAN} courses
 * @param quranScope         populated for {@code QURAN} courses
 * @param bookTitle          populated for {@code BOOK} courses
 * @param bookTotalChapters  populated for {@code BOOK} courses
 * @param bookTotalPages     populated for {@code BOOK} courses
 * @param creationDate       when the course was created
 */
@Schema(name = "Course")
public record CourseDTO(
        Long id,
        Long classId,
        String name,
        CourseType type,
        String description,
        QuranMode quranMode,
        QuranScopeDTO quranScope,
        String bookTitle,
        Integer bookTotalChapters,
        Integer bookTotalPages,
        Instant creationDate) {

    /**
     * Target juz range for a {@code QURAN} course.
     */
    @Schema(name = "QuranScope")
    public record QuranScopeDTO(int fromJuz, int toJuz) {}
}
