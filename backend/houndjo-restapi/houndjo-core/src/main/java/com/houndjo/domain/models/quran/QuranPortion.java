package com.houndjo.domain.models.quran;

/**
 * A contiguous range of the Quran text, bounded by its {@code from}/{@code to} verse
 * references, with the page/juz/hizb span it derives from and covers.
 *
 * @param fromSurah the surah number the range starts in
 * @param fromVerse the verse number the range starts at within {@code fromSurah}
 * @param toSurah   the surah number the range ends in
 * @param toVerse   the verse number the range ends at within {@code toSurah}
 * @param fromPage  the Mushaf page the range starts on
 * @param toPage    the Mushaf page the range ends on
 * @param fromJuz   the juz the range starts in
 * @param toJuz     the juz the range ends in
 * @param fromHizb  the hizb the range starts in
 * @param toHizb    the hizb the range ends in
 */
public record QuranPortion(
        int fromSurah,
        int fromVerse,
        int toSurah,
        int toVerse,
        int fromPage,
        int toPage,
        int fromJuz,
        int toJuz,
        int fromHizb,
        int toHizb) {}
