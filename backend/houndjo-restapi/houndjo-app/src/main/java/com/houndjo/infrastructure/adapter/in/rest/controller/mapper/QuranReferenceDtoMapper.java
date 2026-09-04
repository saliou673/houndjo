package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.models.quran.Surah;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.JuzDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PageDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SurahDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.VerseDTO;
import java.util.List;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Maps Quran reference domain objects to their response DTOs. Surah name localization and the
 * juz/page composition happen here rather than in MapStruct-generated code, since they combine
 * more than one domain value.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface QuranReferenceDtoMapper {

    VerseDTO toDTO(Verse verse);

    List<VerseDTO> toDTO(List<Verse> verses);

    default SurahDTO toDTO(Surah surah, int firstPage, Locale locale) {
        // The app defaults to French (see LocaleConfiguration); English is the only other
        // supported surah name translation for now, so anything not "en" falls back to French.
        String name =
                Locale.ENGLISH.getLanguage().equalsIgnoreCase(locale.getLanguage()) ? surah.nameEn() : surah.nameFr();
        return new SurahDTO(
                surah.number(),
                surah.nameArabic(),
                surah.nameTransliteration(),
                name,
                surah.revelationPlace(),
                surah.verseCount(),
                firstPage);
    }

    default JuzDTO toDTO(QuranPortion portion) {
        return new JuzDTO(
                portion.fromJuz(),
                portion.fromSurah(),
                portion.fromVerse(),
                portion.toSurah(),
                portion.toVerse(),
                portion.fromPage(),
                portion.toPage());
    }

    default List<JuzDTO> toJuzDTO(List<QuranPortion> portions) {
        return portions.stream().map(this::toDTO).toList();
    }

    default PageDTO toPageDTO(int pageNumber, List<Verse> verses) {
        return new PageDTO(pageNumber, toDTO(verses));
    }
}
