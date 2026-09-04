package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The target Quran portion computed for a student's next session on a given flow.
 *
 * @param fromSurah the surah number the portion starts in
 * @param fromVerse the verse number the portion starts at within {@code fromSurah}
 * @param toSurah   the surah number the portion ends in
 * @param toVerse   the verse number the portion ends at within {@code toSurah}
 * @param fromPage  the Mushaf page the portion starts on
 * @param toPage    the Mushaf page the portion ends on
 * @param fromJuz   the juz the portion starts in
 * @param toJuz     the juz the portion ends in
 * @param fromHizb  the hizb the portion starts in
 * @param toHizb    the hizb the portion ends in
 */
@Schema(name = "Portion")
public record PortionDTO(
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
