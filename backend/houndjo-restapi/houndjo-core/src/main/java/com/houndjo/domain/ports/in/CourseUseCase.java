package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.QuranMode;
import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.query.PagedResult;

/**
 * Use case for managing the courses of a class within the active organization.
 */
public interface CourseUseCase {

    /**
     * Returns the courses of a class, paginated.
     *
     * @param classId the owning class identifier
     * @param page    zero-based page index
     * @param size    maximum items per page
     * @return paginated courses
     */
    PagedResult<Course> findByClassId(Long classId, int page, int size);

    /**
     * Returns a course by its identifier within a class.
     *
     * @param classId the owning class identifier
     * @param id      the course identifier
     * @return the matching course
     */
    Course getById(Long classId, Long id);

    /**
     * Creates a new course in a class. The type-specific fields required depend on {@code type}
     * (e.g. {@code QURAN} requires {@code quranMode}, {@code quranScopeFromJuz} and
     * {@code quranScopeToJuz}; {@code BOOK} requires {@code bookTitle}).
     */
    Course create(
            Long classId,
            String name,
            String description,
            CourseType type,
            QuranMode quranMode,
            Integer quranScopeFromJuz,
            Integer quranScopeToJuz,
            String bookTitle,
            Integer bookTotalChapters,
            Integer bookTotalPages);

    /**
     * Updates an existing course of a class. See {@link #create} for the type-specific field
     * requirements.
     */
    Course update(
            Long classId,
            Long id,
            String name,
            String description,
            CourseType type,
            QuranMode quranMode,
            Integer quranScopeFromJuz,
            Integer quranScopeToJuz,
            String bookTitle,
            Integer bookTotalChapters,
            Integer bookTotalPages);

    /**
     * Deletes a course of a class.
     *
     * @param classId the owning class identifier
     * @param id      the course identifier
     */
    void delete(Long classId, Long id);
}
