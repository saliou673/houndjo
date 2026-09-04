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
     * @param toJuz   the last juz of the course's target range
     * @param flow    the pace to apply
     * @return the target portion
     */
    public QuranPortion computeNextPortionFromScopeStart(int fromJuz, int toJuz, PaceFlow flow) {
        QuranPortion scopeStart = quranReferencePort.portionForJuz(fromJuz);
        QuranPortion scopeEnd = quranReferencePort.portionForJuz(toJuz);
        VerseReference start = new VerseReference(scopeStart.fromSurah(), scopeStart.fromVerse());
        VerseReference end = new VerseReference(scopeEnd.toSurah(), scopeEnd.toVerse());
        return computePortionFrom(start, end, flow);
    }

    /**
     * Computes the target portion for the next session, starting at a given position.
     *
     * @param start the current position
     * @param flow  the pace to apply
     * @return the target portion
     */
    public QuranPortion computePortionFrom(VerseReference start, PaceFlow flow) {
        QuranPortion quranEnd = quranReferencePort.portionForJuz(30);
        VerseReference end = new VerseReference(quranEnd.toSurah(), quranEnd.toVerse());
        return computePortionFrom(start, end, flow);
    }

    private QuranPortion computePortionFrom(VerseReference start, VerseReference end, PaceFlow flow) {
        int units = flow.amount()
                .setScale(0, RoundingMode.CEILING)
                .max(BigDecimal.ONE)
                .intValueExact();
        return switch (flow.unit()) {
            case PAGE -> {
                int fromPage = quranReferencePort.pageOf(start.surahNumber(), start.verseNumber());
                int lastPage = quranReferencePort.pageOf(end.surahNumber(), end.verseNumber());
                int toPage = (int) Math.min((long) fromPage + units - 1, lastPage);
                QuranPortion candidate = quranReferencePort.portionForPageRange(fromPage, toPage);
                yield boundedPortion(start, end, candidate);
            }
            case VERSE -> versesPortion(start, end, units);
            case HIZB -> {
                int fromHizb = hizbOf(start);
                int lastHizb = hizbOf(end);
                int toHizb = (int) Math.min((long) fromHizb + units - 1, lastHizb);
                QuranPortion candidate = quranReferencePort.portionForHizbRange(fromHizb, toHizb);
                yield boundedPortion(start, end, candidate);
            }
            case NISF_HIZB -> {
                int fromQuarter = hizbQuarterOf(start);
                int lastQuarter = hizbQuarterOf(end);
                long requestedLastQuarter = (long) fromQuarter + (long) units * 2 - 1;
                int toQuarter = (int) Math.min(requestedLastQuarter, lastQuarter);
                QuranPortion candidate = quranReferencePort.portionForHizbQuarterRange(fromQuarter, toQuarter);
                yield boundedPortion(start, end, candidate);
            }
            case LESSON, CHAPTER ->
                throw new IllegalArgumentException(
                        "PortionCalculator only supports Quran sub-verse units (PAGE, VERSE, HIZB, NISF_HIZB), got "
                                + flow.unit());
        };
    }

    private QuranPortion versesPortion(VerseReference start, VerseReference end, int verseCount) {
        List<Verse> available = quranReferencePort.versesBetween(start, end);
        return toPortion(available.subList(0, Math.min(verseCount, available.size())));
    }

    private QuranPortion boundedPortion(VerseReference start, VerseReference end, QuranPortion candidate) {
        VerseReference candidateEnd = new VerseReference(candidate.toSurah(), candidate.toVerse());
        VerseReference boundedEnd = compare(candidateEnd, end) <= 0 ? candidateEnd : end;
        return toPortion(quranReferencePort.versesBetween(start, boundedEnd));
    }

    private QuranPortion toPortion(List<Verse> range) {
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

    private int compare(VerseReference left, VerseReference right) {
        int surahComparison = Integer.compare(left.surahNumber(), right.surahNumber());
        return surahComparison != 0 ? surahComparison : Integer.compare(left.verseNumber(), right.verseNumber());
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
