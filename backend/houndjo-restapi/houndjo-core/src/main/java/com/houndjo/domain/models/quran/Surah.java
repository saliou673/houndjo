package com.houndjo.domain.models.quran;

import com.houndjo.domain.enumerations.RevelationPlace;
import java.util.Objects;

/**
 * Immutable global reference data for a Quran surah (chapter).
 *
 * @param number               the surah number, 1..114
 * @param nameArabic           the surah name in Arabic
 * @param nameTransliteration  the surah name transliterated in Latin script
 * @param nameFr               the surah name translated in French
 * @param nameEn               the surah name translated in English
 * @param revelationPlace      where the surah was revealed
 * @param verseCount           number of verses (ayahs) in the surah
 */
public record Surah(
        int number,
        String nameArabic,
        String nameTransliteration,
        String nameFr,
        String nameEn,
        RevelationPlace revelationPlace,
        int verseCount) {

    public Surah {
        Objects.requireNonNull(nameArabic, "nameArabic must not be null");
        Objects.requireNonNull(nameTransliteration, "nameTransliteration must not be null");
        Objects.requireNonNull(nameFr, "nameFr must not be null");
        Objects.requireNonNull(nameEn, "nameEn must not be null");
        Objects.requireNonNull(revelationPlace, "revelationPlace must not be null");
    }
}
