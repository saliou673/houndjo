package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.QuranVerseEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link QuranVerseEntity}.
 */
@Transactional(readOnly = true)
public interface QuranVerseRepository extends JpaRepository<QuranVerseEntity, Long> {

    interface SurahFirstPage {

        Short getSurahNumber();

        Short getFirstPage();
    }

    List<QuranVerseEntity> findBySurahNumberOrderByVerseNumberAsc(Short surahNumber);

    Optional<QuranVerseEntity> findBySurahNumberAndVerseNumber(Short surahNumber, Short verseNumber);

    Optional<QuranVerseEntity> findFirstByPageOrderBySurahNumberAscVerseNumberAsc(Short page);

    Optional<QuranVerseEntity> findFirstByPageOrderBySurahNumberDescVerseNumberDesc(Short page);

    Optional<QuranVerseEntity> findFirstByJuzOrderBySurahNumberAscVerseNumberAsc(Short juz);

    Optional<QuranVerseEntity> findFirstByJuzOrderBySurahNumberDescVerseNumberDesc(Short juz);

    Optional<QuranVerseEntity> findFirstByHizbOrderBySurahNumberAscVerseNumberAsc(Short hizb);

    Optional<QuranVerseEntity> findFirstByHizbOrderBySurahNumberDescVerseNumberDesc(Short hizb);

    Optional<QuranVerseEntity> findFirstByHizbQuarterOrderBySurahNumberAscVerseNumberAsc(Short hizbQuarter);

    Optional<QuranVerseEntity> findFirstByHizbQuarterOrderBySurahNumberDescVerseNumberDesc(Short hizbQuarter);

    @Query("""
            SELECT v.surahNumber AS surahNumber, MIN(v.page) AS firstPage
            FROM QuranVerseEntity v
            GROUP BY v.surahNumber
            """)
    List<SurahFirstPage> findFirstPagesBySurah();

    /**
     * Returns every verse between two (surah, verse) references, inclusive, ordered by surah
     * then verse number. The range may span multiple surahs.
     */
    @Query("""
            SELECT v FROM QuranVerseEntity v
            WHERE (v.surahNumber > :fromSurah OR (v.surahNumber = :fromSurah AND v.verseNumber >= :fromVerse))
              AND (v.surahNumber < :toSurah OR (v.surahNumber = :toSurah AND v.verseNumber <= :toVerse))
            ORDER BY v.surahNumber ASC, v.verseNumber ASC
            """)
    List<QuranVerseEntity> findBetween(
            @Param("fromSurah") Short fromSurah,
            @Param("fromVerse") Short fromVerse,
            @Param("toSurah") Short toSurah,
            @Param("toVerse") Short toVerse);
}
