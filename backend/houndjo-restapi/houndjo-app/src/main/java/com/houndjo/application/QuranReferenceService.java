package com.houndjo.application;

import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.models.quran.Surah;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.domain.models.quran.VerseReference;
import com.houndjo.domain.ports.in.QuranReferenceUseCase;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link QuranReferenceUseCase}: read-only Quran reference
 * queries, composed from {@link QuranReferencePort} conversion primitives.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuranReferenceService implements QuranReferenceUseCase {

    private static final int TOTAL_JUZ_COUNT = 30;
    private static final int FIRST_VERSE_NUMBER = 1;

    private final QuranReferencePort quranReferencePort;

    @Override
    public List<Surah> listSurahs() {
        return quranReferencePort.listSurahs();
    }

    @Override
    public List<Verse> versesOfSurah(int surahNumber) {
        return quranReferencePort.versesOfSurah(surahNumber);
    }

    @Override
    public int firstPageOfSurah(int surahNumber) {
        return quranReferencePort.pageOf(surahNumber, FIRST_VERSE_NUMBER);
    }

    @Override
    public List<QuranPortion> listJuz() {
        return IntStream.rangeClosed(1, TOTAL_JUZ_COUNT)
                .mapToObj(quranReferencePort::portionForJuz)
                .toList();
    }

    @Override
    public QuranPortion getJuz(int juzNumber) {
        return quranReferencePort.portionForJuz(juzNumber);
    }

    @Override
    public List<Verse> versesOfPage(int pageNumber) {
        QuranPortion portion = quranReferencePort.portionForPageRange(pageNumber, pageNumber);
        return quranReferencePort.versesBetween(
                new VerseReference(portion.fromSurah(), portion.fromVerse()),
                new VerseReference(portion.toSurah(), portion.toVerse()));
    }
}
