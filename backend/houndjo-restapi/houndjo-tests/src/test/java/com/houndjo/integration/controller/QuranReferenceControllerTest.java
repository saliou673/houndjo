package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.RevelationPlace;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.JuzDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PageDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SurahDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.VerseDTO;
import com.houndjo.integration.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Verifies the public, read-only Quran reference API: surahs, verses, juz and page lookups.
 */
class QuranReferenceControllerTest extends IntegrationTest {

    private static final String API = "/api/quran";

    @Test
    void shouldListAllSurahsAnonymously() throws Exception {
        List<SurahDTO> surahs = get(API + "/surahs", new TypeReference<>() {}, status().isOk());

        assertThat(surahs).hasSize(114);
        assertThat(surahs.getFirst().number()).isEqualTo(1);
        assertThat(surahs.getFirst().nameArabic()).isEqualTo("الفاتحة");
        assertThat(surahs.getFirst().verseCount()).isEqualTo(7);
        assertThat(surahs.getFirst().firstPage()).isEqualTo(1);
        assertThat(surahs.getFirst().revelationPlace()).isEqualTo(RevelationPlace.MECCAN);
    }

    @Test
    void shouldLocalizeSurahNamesByAcceptLanguageKeepingArabicIdentical() throws Exception {
        String frResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get(API + "/surahs").header("Accept-Language", "fr"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String enResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get(API + "/surahs").header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<SurahDTO> frSurahs = objectMapper.readValue(frResponse, new TypeReference<>() {});
        List<SurahDTO> enSurahs = objectMapper.readValue(enResponse, new TypeReference<>() {});

        SurahDTO frFatiha = frSurahs.getFirst();
        SurahDTO enFatiha = enSurahs.getFirst();

        assertThat(frFatiha.name()).isEqualTo("L'ouverture");
        assertThat(enFatiha.name()).isEqualTo("The Opener");
        assertThat(frFatiha.name()).isNotEqualTo(enFatiha.name());
        assertThat(frFatiha.nameArabic()).isEqualTo(enFatiha.nameArabic());
    }

    @Test
    void shouldReturnVersesOfSurah() throws Exception {
        List<VerseDTO> verses = get(API + "/surahs/1/verses", new TypeReference<>() {}, status().isOk());

        assertThat(verses).hasSize(7);
        assertThat(verses.getFirst().surahNumber()).isEqualTo(1);
        assertThat(verses.getFirst().verseNumber()).isEqualTo(1);
        assertThat(verses.getLast().verseNumber()).isEqualTo(7);
    }

    @Test
    void shouldReturn404ForUnknownSurah() throws Exception {
        get(API + "/surahs/115/verses", status().isNotFound());
    }

    @Test
    void shouldListAllThirtyJuz() throws Exception {
        List<JuzDTO> juz = get(API + "/juz", new TypeReference<>() {}, status().isOk());

        assertThat(juz).hasSize(30);
        assertThat(juz.getFirst().number()).isEqualTo(1);
        assertThat(juz.getLast().number()).isEqualTo(30);
    }

    @Test
    void shouldReturnLastJuzEndingAtLastSurahs() throws Exception {
        JuzDTO juz30 = get(API + "/juz/30", JuzDTO.class, status().isOk());

        assertThat(juz30.number()).isEqualTo(30);
        assertThat(juz30.toSurah()).isEqualTo(114);
        assertThat(juz30.toPage()).isEqualTo(604);
    }

    @Test
    void shouldReturn404ForUnknownJuz() throws Exception {
        get(API + "/juz/31", status().isNotFound());
    }

    @Test
    void shouldReturnVersesOfFirstPage() throws Exception {
        PageDTO page = get(API + "/pages/1", PageDTO.class, status().isOk());

        assertThat(page.number()).isEqualTo(1);
        assertThat(page.verses()).isNotEmpty();
        assertThat(page.verses().getFirst().surahNumber()).isEqualTo(1);
        assertThat(page.verses().getFirst().verseNumber()).isEqualTo(1);
    }

    @Test
    void shouldReturn404ForUnknownPage() throws Exception {
        get(API + "/pages/605", status().isNotFound());
    }
}
