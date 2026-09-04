package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO representing a juz (part) of the Quran and the page/verse range it spans.
 *
 * @param number    the juz number, 1..30
 * @param fromSurah the surah number the juz starts in
 * @param fromVerse the verse number the juz starts at within {@code fromSurah}
 * @param toSurah   the surah number the juz ends in
 * @param toVerse   the verse number the juz ends at within {@code toSurah}
 * @param fromPage  the Mushaf page the juz starts on
 * @param toPage    the Mushaf page the juz ends on
 */
@Schema(name = "Juz")
public record JuzDTO(int number, int fromSurah, int fromVerse, int toSurah, int toVerse, int fromPage, int toPage) {}
