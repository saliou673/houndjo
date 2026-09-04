package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.models.quran.Surah;
import com.houndjo.domain.models.quran.Verse;
import java.util.List;

/**
 * Use case for querying the immutable, global Quran reference data.
 */
public interface QuranReferenceUseCase {

    /**
     * Returns every surah, ordered by number.
     */
    List<Surah> listSurahs();

    /**
     * Returns every verse of a surah, ordered by verse number.
     *
     * @param surahNumber the surah number, 1..114
     * @throws com.houndjo.domain.exceptions.SurahNotFoundException if no surah has this number
     */
    List<Verse> versesOfSurah(int surahNumber);

    /**
     * Returns the Mushaf page a surah opens on.
     *
     * @param surahNumber the surah number, 1..114
     * @throws com.houndjo.domain.exceptions.VerseNotFoundException if no surah has this number
     */
    int firstPageOfSurah(int surahNumber);

    /**
     * Returns the portion covered by every juz, in order (juz 1 to 30).
     */
    List<QuranPortion> listJuz();

    /**
     * Returns the portion covered by a single juz.
     *
     * @param juzNumber the juz number, 1..30
     * @throws com.houndjo.domain.exceptions.JuzNotFoundException if no verse belongs to this juz
     */
    QuranPortion getJuz(int juzNumber);

    /**
     * Returns every verse appearing on a Mushaf page, ordered by surah then verse number.
     *
     * @param pageNumber the page number, 1..604
     * @throws com.houndjo.domain.exceptions.PageNotFoundException if no verse is on this page
     */
    List<Verse> versesOfPage(int pageNumber);
}
