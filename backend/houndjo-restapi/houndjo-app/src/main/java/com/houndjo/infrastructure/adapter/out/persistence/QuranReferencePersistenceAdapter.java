package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.exceptions.JuzNotFoundException;
import com.houndjo.domain.exceptions.PageNotFoundException;
import com.houndjo.domain.exceptions.SurahNotFoundException;
import com.houndjo.domain.exceptions.VerseNotFoundException;
import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.models.quran.Surah;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.domain.models.quran.VerseReference;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.QuranVerseEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.QuranReferenceMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.QuranSurahRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.QuranVerseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * JPA adapter implementing {@link QuranReferencePort}.
 */
@Service
@RequiredArgsConstructor
public class QuranReferencePersistenceAdapter implements QuranReferencePort {

    private static final int FIRST_SURAH_NUMBER = 1;
    private static final int LAST_SURAH_NUMBER = 114;
    private static final int FIRST_PAGE_NUMBER = 1;
    private static final int LAST_PAGE_NUMBER = 604;
    private static final int FIRST_JUZ_NUMBER = 1;
    private static final int LAST_JUZ_NUMBER = 30;

    private final QuranSurahRepository quranSurahRepository;
    private final QuranVerseRepository quranVerseRepository;
    private final QuranReferenceMapper quranReferenceMapper;

    @Override
    public List<Surah> listSurahs() {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> quranReferenceMapper.toDomainSurahs(
                        quranSurahRepository.findAll(Sort.by(Sort.Direction.ASC, "number"))),
                "Error fetching all surahs");
    }

    @Override
    public Surah getSurah(int number) {
        requireValidSurahNumber(number);
        return AdapterPersistenceUtils.executeDbOperation(
                () -> quranSurahRepository
                        .findById((short) number)
                        .map(quranReferenceMapper::toDomain)
                        .orElseThrow(() -> new SurahNotFoundException(number)),
                "Error fetching surah");
    }

    @Override
    public List<Verse> versesOfSurah(int surahNumber) {
        requireValidSurahNumber(surahNumber);
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    if (!quranSurahRepository.existsById((short) surahNumber)) {
                        throw new SurahNotFoundException(surahNumber);
                    }
                    return quranReferenceMapper.toDomainVerses(
                            quranVerseRepository.findBySurahNumberOrderByVerseNumberAsc((short) surahNumber));
                },
                "Error fetching verses of surah");
    }

    @Override
    public QuranPortion portionForPageRange(int fromPage, int toPage) {
        requireValidPageNumber(fromPage);
        requireValidPageNumber(toPage);
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    QuranVerseEntity first = quranVerseRepository
                            .findFirstByPageOrderBySurahNumberAscVerseNumberAsc((short) fromPage)
                            .orElseThrow(() -> new PageNotFoundException(fromPage));
                    QuranVerseEntity last = quranVerseRepository
                            .findFirstByPageOrderBySurahNumberDescVerseNumberDesc((short) toPage)
                            .orElseThrow(() -> new PageNotFoundException(toPage));
                    return new QuranPortion(
                            first.getSurahNumber().intValue(),
                            first.getVerseNumber().intValue(),
                            last.getSurahNumber().intValue(),
                            last.getVerseNumber().intValue(),
                            fromPage,
                            toPage,
                            first.getJuz().intValue(),
                            last.getJuz().intValue(),
                            first.getHizb().intValue(),
                            last.getHizb().intValue());
                },
                "Error computing portion for page range");
    }

    @Override
    public QuranPortion portionForJuz(int juzNumber) {
        requireValidJuzNumber(juzNumber);
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    QuranVerseEntity first = quranVerseRepository
                            .findFirstByJuzOrderBySurahNumberAscVerseNumberAsc((short) juzNumber)
                            .orElseThrow(() -> new JuzNotFoundException(juzNumber));
                    QuranVerseEntity last = quranVerseRepository
                            .findFirstByJuzOrderBySurahNumberDescVerseNumberDesc((short) juzNumber)
                            .orElseThrow(() -> new JuzNotFoundException(juzNumber));
                    return new QuranPortion(
                            first.getSurahNumber().intValue(),
                            first.getVerseNumber().intValue(),
                            last.getSurahNumber().intValue(),
                            last.getVerseNumber().intValue(),
                            first.getPage().intValue(),
                            last.getPage().intValue(),
                            juzNumber,
                            juzNumber,
                            first.getHizb().intValue(),
                            last.getHizb().intValue());
                },
                "Error computing portion for juz");
    }

    @Override
    public List<Verse> versesBetween(VerseReference from, VerseReference to) {
        requireValidVerseReference(from);
        requireValidVerseReference(to);
        return AdapterPersistenceUtils.executeDbOperation(
                () -> quranReferenceMapper.toDomainVerses(quranVerseRepository.findBetween(
                        (short) from.surahNumber(), (short) from.verseNumber(), (short) to.surahNumber(), (short)
                                to.verseNumber())),
                "Error fetching verses between references");
    }

    @Override
    public int pageOf(int surahNumber, int verseNumber) {
        if (surahNumber < FIRST_SURAH_NUMBER
                || surahNumber > LAST_SURAH_NUMBER
                || verseNumber < 1
                || verseNumber > Short.MAX_VALUE) {
            throw new VerseNotFoundException(surahNumber, verseNumber);
        }
        return AdapterPersistenceUtils.executeDbOperation(
                () -> quranVerseRepository
                        .findBySurahNumberAndVerseNumber((short) surahNumber, (short) verseNumber)
                        .map(entity -> entity.getPage().intValue())
                        .orElseThrow(() -> new VerseNotFoundException(surahNumber, verseNumber)),
                "Error fetching page of verse");
    }

    private static void requireValidSurahNumber(int number) {
        if (number < FIRST_SURAH_NUMBER || number > LAST_SURAH_NUMBER) {
            throw new SurahNotFoundException(number);
        }
    }

    private static void requireValidPageNumber(int number) {
        if (number < FIRST_PAGE_NUMBER || number > LAST_PAGE_NUMBER) {
            throw new PageNotFoundException(number);
        }
    }

    private static void requireValidJuzNumber(int number) {
        if (number < FIRST_JUZ_NUMBER || number > LAST_JUZ_NUMBER) {
            throw new JuzNotFoundException(number);
        }
    }

    private static void requireValidVerseReference(VerseReference reference) {
        if (reference.surahNumber() < FIRST_SURAH_NUMBER
                || reference.surahNumber() > LAST_SURAH_NUMBER
                || reference.verseNumber() < 1
                || reference.verseNumber() > Short.MAX_VALUE) {
            throw new VerseNotFoundException(reference.surahNumber(), reference.verseNumber());
        }
    }
}
