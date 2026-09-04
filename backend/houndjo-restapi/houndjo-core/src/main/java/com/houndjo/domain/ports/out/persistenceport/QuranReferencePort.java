package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.models.quran.Surah;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.domain.models.quran.VerseReference;
import java.util.List;

/**
 * Read-only persistence port over the immutable, global Quran reference data (surahs and
 * verses), backing page/juz/hizb/verse conversions used by course scoping, pace calculation and
 * progress tracking.
 */
public interface QuranReferencePort {

    /**
     * Returns every surah, ordered by number.
     */
    List<Surah> listSurahs();

    /**
     * Returns the surah with the given number.
     *
     * @param number the surah number, 1..114
     * @return the matching surah
     * @throws com.houndjo.domain.exceptions.SurahNotFoundException if no surah has this number
     */
    Surah getSurah(int number);

    /**
     * Returns all verses of a surah, ordered by verse number.
     *
     * @param surahNumber the surah number, 1..114
     * @return the surah's verses in order
     * @throws com.houndjo.domain.exceptions.SurahNotFoundException if no surah has this number
     */
    List<Verse> versesOfSurah(int surahNumber);

    /**
     * Computes the portion of the Quran spanning the given page range.
     *
     * @param fromPage the first page, 1..604
     * @param toPage   the last page, 1..604
     * @return the portion covering that page range
     * @throws com.houndjo.domain.exceptions.PageNotFoundException if either page has no verse
     */
    QuranPortion portionForPageRange(int fromPage, int toPage);

    /**
     * Computes the portion of the Quran spanning a whole juz.
     *
     * @param juzNumber the juz number, 1..30
     * @return the portion covering that juz
     * @throws com.houndjo.domain.exceptions.JuzNotFoundException if no verse belongs to this juz
     */
    QuranPortion portionForJuz(int juzNumber);

    /**
     * Returns every verse between two verse references, inclusive, ordered by surah then verse
     * number. The range may span multiple surahs.
     *
     * @param from the starting verse reference
     * @param to   the ending verse reference
     * @return the verses in that range, in order
     */
    List<Verse> versesBetween(VerseReference from, VerseReference to);

    /**
     * Returns the Mushaf page a given verse appears on.
     *
     * @param surahNumber the surah number, 1..114
     * @param verseNumber the verse number within the surah
     * @return the page number, 1..604
     * @throws com.houndjo.domain.exceptions.VerseNotFoundException if no such verse exists
     */
    int pageOf(int surahNumber, int verseNumber);
}
