package com.houndjo.domain.models.quran;

/**
 * Immutable global reference data for a single Quran verse (ayah), positioned within the
 * 604-page Medina Mushaf layout.
 *
 * @param surahNumber the surah this verse belongs to, 1..114
 * @param verseNumber the verse (ayah) number within its surah
 * @param page        the Mushaf page the verse appears on, 1..604
 * @param juz         the juz (part) the verse belongs to, 1..30
 * @param hizb        the hizb the verse belongs to, 1..60
 * @param hizbQuarter the rub' al-hizb (quarter-hizb) the verse belongs to, 1..240
 */
public record Verse(int surahNumber, int verseNumber, int page, int juz, int hizb, int hizbQuarter) {}
