package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.ports.in.QuranReferenceUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.JuzDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PageDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SurahDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.VerseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.QuranReferenceDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only Quran reference data: surahs, verses, juz and page conversions. Immutable, global
 * data — reachable without authentication (see {@code SecurityConfiguration.PUBLIC_ROUTES}).
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Quran reference data")
@RequestMapping(path = "/api/v1/quran", version = "1.0")
public class QuranReferenceController {

    private final QuranReferenceUseCase quranReferenceUseCase;
    private final QuranReferenceDtoMapper quranReferenceDtoMapper;

    @GetMapping("/surahs")
    public List<SurahDTO> getSurahs() {
        Map<Integer, Integer> firstPages = quranReferenceUseCase.firstPagesOfSurahs();
        return quranReferenceUseCase.listSurahs().stream()
                .map(surah -> quranReferenceDtoMapper.toDTO(
                        surah, firstPages.get(surah.number()), LocaleContextHolder.getLocale()))
                .toList();
    }

    @GetMapping("/surahs/{number}/verses")
    public List<VerseDTO> getVersesOfSurah(@PathVariable int number) {
        return quranReferenceDtoMapper.toDTO(quranReferenceUseCase.versesOfSurah(number));
    }

    @GetMapping("/juz")
    public List<JuzDTO> getJuz() {
        return quranReferenceDtoMapper.toJuzDTO(quranReferenceUseCase.listJuz());
    }

    @GetMapping("/juz/{number}")
    public JuzDTO getJuzByNumber(@PathVariable int number) {
        return quranReferenceDtoMapper.toDTO(quranReferenceUseCase.getJuz(number));
    }

    @GetMapping("/pages/{number}")
    public PageDTO getPage(@PathVariable int number) {
        return quranReferenceDtoMapper.toPageDTO(number, quranReferenceUseCase.versesOfPage(number));
    }
}
