package com.houndjo.domain.models.quran;

/**
 * Points at a single verse (ayah) by its surah and verse number, used to bound a range query
 * (e.g. {@code QuranReferencePort#versesBetween}) without carrying the full {@link Verse} data.
 *
 * @param surahNumber the surah number, 1..114
 * @param verseNumber the verse (ayah) number within its surah
 */
public record VerseReference(int surahNumber, int verseNumber) {}
