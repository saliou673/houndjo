package com.houndjo.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.houndjo.domain.enumerations.RevelationPlace;
import com.houndjo.domain.exceptions.JuzNotFoundException;
import com.houndjo.domain.exceptions.PageNotFoundException;
import com.houndjo.domain.exceptions.SurahNotFoundException;
import com.houndjo.domain.exceptions.VerseNotFoundException;
import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.models.quran.Surah;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.domain.models.quran.VerseReference;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import com.houndjo.infrastructure.adapter.out.persistence.repository.QuranSurahRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.QuranVerseRepository;
import com.houndjo.integration.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verifies the seeded Quran reference data (114 surahs, 6236 verses) and the
 * {@link QuranReferencePort} conversions built on top of it.
 */
class QuranReferencePersistenceAdapterTest extends IntegrationTest {

    @Autowired
    private QuranReferencePort quranReferencePort;

    @Autowired
    private QuranSurahRepository quranSurahRepository;

    @Autowired
    private QuranVerseRepository quranVerseRepository;

    @Test
    void shouldHaveExactReferenceDataCounts() {
        assertThat(quranSurahRepository.count()).isEqualTo(114);
        assertThat(quranVerseRepository.count()).isEqualTo(6236);
        assertThat(quranVerseRepository.findAll().stream()
                        .mapToInt(v -> v.getPage())
                        .max())
                .hasValue(604);
        assertThat(quranVerseRepository.findAll().stream()
                        .mapToInt(v -> v.getJuz())
                        .max())
                .hasValue(30);
        assertThat(quranVerseRepository.findAll().stream()
                        .mapToInt(v -> v.getHizb())
                        .max())
                .hasValue(60);
    }

    @Test
    void shouldHaveExactlyVerseCountVersesPerSurah() {
        for (int number = 1; number <= 114; number++) {
            Surah surah = quranReferencePort.getSurah(number);
            List<Verse> verses = quranReferencePort.versesOfSurah(number);
            assertThat(verses).as("surah %d verse count", number).hasSize(surah.verseCount());
        }
    }

    @Test
    void shouldReturnAlFatihaWithArabicAndLocalizedNames() {
        Surah surah = quranReferencePort.getSurah(1);

        assertThat(surah.number()).isEqualTo(1);
        assertThat(surah.nameArabic()).isEqualTo("الفاتحة");
        assertThat(surah.nameTransliteration()).isEqualTo("Al-Fatihah");
        assertThat(surah.nameFr()).isEqualTo("L'ouverture");
        assertThat(surah.nameEn()).isEqualTo("The Opener");
        assertThat(surah.revelationPlace()).isEqualTo(RevelationPlace.MECCAN);
        assertThat(surah.verseCount()).isEqualTo(7);
    }

    @Test
    void shouldThrowWhenSurahNumberDoesNotExist() {
        assertThatThrownBy(() -> quranReferencePort.getSurah(115)).isInstanceOf(SurahNotFoundException.class);
        assertThatThrownBy(() -> quranReferencePort.getSurah(65537)).isInstanceOf(SurahNotFoundException.class);
        assertThatThrownBy(() -> quranReferencePort.versesOfSurah(0)).isInstanceOf(SurahNotFoundException.class);
    }

    @Test
    void shouldResolveKnownPageToSurahVerseRange() {
        // Page 50 of the Medina Mushaf opens on Aal-i-Imraan 3:1 and ends at 3:9.
        QuranPortion portion = quranReferencePort.portionForPageRange(50, 50);

        assertThat(portion.fromSurah()).isEqualTo(3);
        assertThat(portion.fromVerse()).isEqualTo(1);
        assertThat(portion.toSurah()).isEqualTo(3);
        assertThat(portion.toVerse()).isEqualTo(9);
    }

    @Test
    void shouldResolveKnownJuzToPageRange() {
        // Juz 5 spans pages 82 to 101 of the Medina Mushaf.
        QuranPortion portion = quranReferencePort.portionForJuz(5);

        assertThat(portion.fromPage()).isEqualTo(82);
        assertThat(portion.toPage()).isEqualTo(101);
        assertThat(portion.fromJuz()).isEqualTo(5);
        assertThat(portion.toJuz()).isEqualTo(5);
    }

    @Test
    void shouldThrowWhenPageOrJuzOutOfRange() {
        assertThatThrownBy(() -> quranReferencePort.portionForPageRange(1, 605))
                .isInstanceOf(PageNotFoundException.class);
        assertThatThrownBy(() -> quranReferencePort.portionForPageRange(65537, 65537))
                .isInstanceOf(PageNotFoundException.class);
        assertThatThrownBy(() -> quranReferencePort.portionForJuz(31)).isInstanceOf(JuzNotFoundException.class);
        assertThatThrownBy(() -> quranReferencePort.portionForJuz(65537)).isInstanceOf(JuzNotFoundException.class);
    }

    @Test
    void shouldResolveKnownVerseToPage() {
        // Ayat al-Kursi (2:255) is a well-known reference point, on page 42 of the Medina Mushaf.
        assertThat(quranReferencePort.pageOf(2, 255)).isEqualTo(42);
    }

    @Test
    void shouldThrowWhenVerseDoesNotExist() {
        assertThatThrownBy(() -> quranReferencePort.pageOf(1, 8)).isInstanceOf(VerseNotFoundException.class);
        assertThatThrownBy(() -> quranReferencePort.pageOf(65538, 255)).isInstanceOf(VerseNotFoundException.class);
    }

    @Test
    void shouldReturnVersesBetweenReferencesSpanningSurahs() {
        // Al-Fatiha 1:5-7 (3 verses) followed by Al-Baqara 2:1-2 (2 verses).
        List<Verse> verses = quranReferencePort.versesBetween(new VerseReference(1, 5), new VerseReference(2, 2));

        assertThat(verses).hasSize(5);
        assertThat(verses.get(0).surahNumber()).isEqualTo(1);
        assertThat(verses.get(0).verseNumber()).isEqualTo(5);
        assertThat(verses.get(4).surahNumber()).isEqualTo(2);
        assertThat(verses.get(4).verseNumber()).isEqualTo(2);
    }
}
