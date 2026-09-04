package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.RevelationPlace;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO representing a Quran surah, with its name localized to the request locale.
 *
 * @param number              the surah number, 1..114
 * @param nameArabic          the surah name in Arabic (always present, regardless of locale)
 * @param nameTransliteration the surah name transliterated in Latin script
 * @param name                the surah name, localized to the request locale
 * @param revelationPlace     where the surah was revealed
 * @param verseCount          number of verses (ayahs) in the surah
 * @param firstPage           the Mushaf page the surah opens on
 */
@Schema(name = "Surah")
public record SurahDTO(
        int number,
        String nameArabic,
        String nameTransliteration,
        String name,
        RevelationPlace revelationPlace,
        int verseCount,
        int firstPage) {}
