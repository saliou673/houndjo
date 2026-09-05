package com.houndjo.domain.models.progress;

import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Computes a student's {@link ProgressState} for one course from their validated
 * {@link ProgressRecord} history. Only {@code VALIDATED} records are considered: an
 * {@code IN_PROGRESS} entry is provisional and doesn't yet count as memorized or revised.
 * Backed by {@link QuranReferencePort} to resolve which juz a recorded verse range belongs to.
 */
public class ProgressStateCalculator {

    private final QuranReferencePort quranReferencePort;

    public ProgressStateCalculator(QuranReferencePort quranReferencePort) {
        this.quranReferencePort = Objects.requireNonNull(quranReferencePort, "quranReferencePort must not be null");
    }

    /**
     * Computes the progress state for one (student, course) pair.
     *
     * @param records       the student's progress records for the course (any flow, any status)
     * @param fromJuz       the first juz of the course's Quran scope
     * @param toJuz         the last juz of the course's Quran scope
     * @param dhorCycleDays the course's configured Dhor revision cycle, in days; a juz whose last
     *                      revision exceeds this many days is stale
     * @param now           the instant to measure staleness against
     * @return the computed progress state
     */
    public ProgressState compute(List<ProgressRecord> records, int fromJuz, int toJuz, int dhorCycleDays, Instant now) {
        FlowSnapshot sabak = latestValidatedSnapshot(records, ProgressFlow.SABAK);
        FlowSnapshot sabqi = latestValidatedSnapshot(records, ProgressFlow.SABQI);
        Map<Integer, Instant> lastDhorReviewByJuz = lastDhorReviewByJuz(records, fromJuz, toJuz);

        List<Integer> coveredJuz =
                lastDhorReviewByJuz.keySet().stream().sorted().toList();
        List<StaleDhorPortion> stalePortions = lastDhorReviewByJuz.entrySet().stream()
                .map(entry -> new StaleDhorPortion(
                        entry.getKey(),
                        entry.getValue(),
                        Duration.between(entry.getValue(), now).toDays()))
                .filter(portion -> portion.daysSince() > dhorCycleDays)
                .sorted(Comparator.comparingInt(StaleDhorPortion::juz))
                .toList();

        return new ProgressState(sabak, sabqi, coveredJuz, stalePortions);
    }

    private FlowSnapshot latestValidatedSnapshot(List<ProgressRecord> records, ProgressFlow flow) {
        return records.stream()
                .filter(record -> record.getFlow() == flow)
                .filter(record -> record.getStatus() == ProgressStatus.VALIDATED)
                .filter(record -> record.getPortion() instanceof QuranPortionRef)
                .max(Comparator.comparing(ProgressRecord::getCreationDate))
                .map(record -> new FlowSnapshot((QuranPortionRef) record.getPortion(), record.getCreationDate()))
                .orElse(null);
    }

    private Map<Integer, Instant> lastDhorReviewByJuz(List<ProgressRecord> records, int fromJuz, int toJuz) {
        Map<Integer, Instant> lastReviewByJuz = new HashMap<>();
        records.stream()
                .filter(record -> record.getFlow() == ProgressFlow.DHOR)
                .filter(record -> record.getStatus() == ProgressStatus.VALIDATED)
                .filter(record -> record.getPortion() instanceof QuranPortionRef)
                .forEach(record -> {
                    QuranPortionRef portion = (QuranPortionRef) record.getPortion();
                    int startJuz = Math.max(fromJuz, juzOf(portion.fromSurah(), portion.fromVerse()));
                    int endJuz = Math.min(toJuz, juzOf(portion.toSurah(), portion.toVerse()));
                    for (int juz = startJuz; juz <= endJuz; juz++) {
                        lastReviewByJuz.merge(
                                juz,
                                record.getCreationDate(),
                                (existing, candidate) -> existing.isAfter(candidate) ? existing : candidate);
                    }
                });
        return lastReviewByJuz;
    }

    private int juzOf(int surahNumber, int verseNumber) {
        return quranReferencePort.versesOfSurah(surahNumber).stream()
                .filter(verse -> verse.verseNumber() == verseNumber)
                .findFirst()
                .orElseThrow()
                .juz();
    }
}
