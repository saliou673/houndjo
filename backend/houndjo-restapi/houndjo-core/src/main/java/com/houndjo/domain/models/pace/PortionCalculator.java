package com.houndjo.domain.models.pace;

import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.domain.models.quran.VerseReference;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Converts a Quran pace ({@link PaceFlow}) plus a starting position into the target
 * {@link QuranPortion} for the next session. Backed by {@link QuranReferencePort} for
 * page/juz/hizb/verse conversions.
 * <p>
 * The reference data has no sub-unit granularity below a single page/verse/hizb/quarter-hizb, so
 * a fractional {@code amount} (e.g. {@code 0.5} page) is rounded up to a whole unit.
 */
public class PortionCalculator {

    private final QuranReferencePort quranReferencePort;

    public PortionCalculator(QuranReferencePort quranReferencePort) {
        this.quranReferencePort = Objects.requireNonNull(quranReferencePort, "quranReferencePort must not be null");
    }

    /**
     * Computes the target portion for the next session, starting at the beginning of a course's
     * Quran scope.
     *
     * @param fromJuz the first juz of the course's target range
     * @param flow    the pace to apply
     * @return the target portion
     */
    public QuranPortion computeNextPortionFromScopeStart(int fromJuz, PaceFlow flow) {
        QuranPortion scopeStart = quranReferencePort.portionForJuz(fromJuz);
        VerseReference start = new VerseReference(scopeStart.fromSurah(), scopeStart.fromVerse());
        return computePortionFrom(start, flow);
    }

    /**
     * Computes the target portion for the next session, starting at a given position.
     *
     * @param start the current position
     * @param flow  the pace to apply
     * @return the target portion
     */
    public QuranPortion computePortionFrom(VerseReference start, PaceFlow flow) {
        int units = flow.amount()
                .setScale(0, RoundingMode.CEILING)
                .max(BigDecimal.ONE)
                .intValueExact();
        return switch (flow.unit()) {
            case PAGE -> {
                int fromPage = quranReferencePort.pageOf(start.surahNumber(), start.verseNumber());
                yield quranReferencePort.portionForPageRange(fromPage, fromPage + units - 1);
            }
            case VERSE -> versesPortion(start, units);
            case HIZB -> {
                int fromHizb = hizbOf(start);
                yield quranReferencePort.portionForHizbRange(fromHizb, fromHizb + units - 1);
            }
            case NISF_HIZB -> {
                int fromQuarter = hizbQuarterOf(start);
                int quarters = units * 2;
                yield quranReferencePort.portionForHizbQuarterRange(fromQuarter, fromQuarter + quarters - 1);
            }
            case LESSON, CHAPTER ->
                throw new IllegalArgumentException(
                        "PortionCalculator only supports Quran sub-verse units (PAGE, VERSE, HIZB, NISF_HIZB), got "
                                + flow.unit());
        };
    }

    private QuranPortion versesPortion(VerseReference start, int verseCount) {
        int surahNumber = start.surahNumber();
        int verseNumber = start.verseNumber();
        int remaining = verseCount - 1;
        while (remaining > 0) {
            List<Verse> versesOfSurah = quranReferencePort.versesOfSurah(surahNumber);
            int versesLeftInSurah = versesOfSurah.size() - verseNumber;
            if (remaining <= versesLeftInSurah) {
                verseNumber += remaining;
                remaining = 0;
            } else {
                remaining -= versesLeftInSurah;
                surahNumber += 1;
                verseNumber = 1;
            }
        }
        List<Verse> range = quranReferencePort.versesBetween(start, new VerseReference(surahNumber, verseNumber));
        Verse first = range.get(0);
        Verse last = range.get(range.size() - 1);
        return new QuranPortion(
                first.surahNumber(),
                first.verseNumber(),
                last.surahNumber(),
                last.verseNumber(),
                first.page(),
                last.page(),
                first.juz(),
                last.juz(),
                first.hizb(),
                last.hizb());
    }

    private int hizbOf(VerseReference reference) {
        return quranReferencePort.versesOfSurah(reference.surahNumber()).stream()
                .filter(verse -> verse.verseNumber() == reference.verseNumber())
                .findFirst()
                .orElseThrow()
                .hizb();
    }

    private int hizbQuarterOf(VerseReference reference) {
        return quranReferencePort.versesOfSurah(reference.surahNumber()).stream()
                .filter(verse -> verse.verseNumber() == reference.verseNumber())
                .findFirst()
                .orElseThrow()
                .hizbQuarter();
    }
}
