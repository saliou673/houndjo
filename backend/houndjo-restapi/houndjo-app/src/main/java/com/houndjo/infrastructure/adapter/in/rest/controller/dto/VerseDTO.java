package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO representing a single Quran verse (ayah) and its position in the Mushaf.
 *
 * @param surahNumber the surah this verse belongs to, 1..114
 * @param verseNumber the verse (ayah) number within its surah
 * @param page        the Mushaf page the verse appears on, 1..604
 * @param juz         the juz (part) the verse belongs to, 1..30
 * @param hizb        the hizb the verse belongs to, 1..60
 */
@Schema(name = "Verse")
public record VerseDTO(int surahNumber, int verseNumber, int page, int juz, int hizb) {}
