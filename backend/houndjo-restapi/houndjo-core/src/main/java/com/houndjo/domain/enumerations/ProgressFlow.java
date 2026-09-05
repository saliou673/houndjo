package com.houndjo.domain.enumerations;

/**
 * The tracking flow a {@link com.houndjo.domain.models.progress.ProgressRecord} belongs to.
 * {@code SABAK}/{@code SABQI}/{@code DHOR} apply to {@code QURAN} courses (new lesson, recent
 * review, long-term revision); {@code LESSON} applies to {@code QAIDA} courses; {@code CHAPTER}
 * applies to {@code BOOK} courses.
 */
public enum ProgressFlow {
    SABAK,
    SABQI,
    DHOR,
    LESSON,
    CHAPTER
}
