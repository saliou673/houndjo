package com.houndjo.domain.models.progress;

/**
 * The verse range worked on for a Quran flow ({@code SABAK}/{@code SABQI}/{@code DHOR}).
 * Existence of the range against the Quran reference data (E2) is verified by the application
 * layer, which has access to {@code QuranReferencePort}; this record only carries the range.
 *
 * @param fromSurah the surah number the range starts in
 * @param fromVerse the verse number the range starts at within {@code fromSurah}
 * @param toSurah   the surah number the range ends in
 * @param toVerse   the verse number the range ends at within {@code toSurah}
 */
public record QuranPortionRef(int fromSurah, int fromVerse, int toSurah, int toVerse) implements PortionRef {}
