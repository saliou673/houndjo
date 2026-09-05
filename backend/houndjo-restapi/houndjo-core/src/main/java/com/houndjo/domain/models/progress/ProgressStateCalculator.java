package com.houndjo.domain.models.progress;

import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.domain.models.quran.VerseReference;
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
        Map<Verse, Instant> firstMemorized = new HashMap<>();
        Map<Verse, Instant> lastReviewed = new HashMap<>();
        Map<QuranPortionRef, List<Verse>> portions = new HashMap<>();
        for (ProgressRecord record : records) {
            if (record.getStatus() != ProgressStatus.VALIDATED
                    || !(record.getPortion() instanceof QuranPortionRef portion)) {
                continue;
            }
            List<Verse> verses = portions.computeIfAbsent(
                    portion,
                    ref -> quranReferencePort.versesBetween(
                            new VerseReference(ref.fromSurah(), ref.fromVerse()),
                            new VerseReference(ref.toSurah(), ref.toVerse())));
            for (Verse verse : verses) {
                if (verse.juz() < fromJuz || verse.juz() > toJuz) {
                    continue;
                }
                if (record.getFlow() == ProgressFlow.DHOR) {
                    lastReviewed.merge(
                            verse, record.getCreationDate(), (left, right) -> left.isAfter(right) ? left : right);
                } else if (record.getFlow() == ProgressFlow.SABAK || record.getFlow() == ProgressFlow.SABQI) {
                    firstMemorized.merge(
                            verse, record.getCreationDate(), (left, right) -> left.isBefore(right) ? left : right);
                }
            }
        }
        List<Integer> coveredJuz = lastReviewed.keySet().stream()
                .map(Verse::juz)
                .distinct()
                .sorted()
                .toList();
        // A partial review refreshes only its verses. Never-reviewed memorized verses become
        // overdue from their first validation, while retaining a null lastReviewedDate.
        Map<Verse, Instant> dueSince = new HashMap<>(firstMemorized);
        dueSince.putAll(lastReviewed);
        Map<Integer, Verse> oldestStaleVerseByJuz = new HashMap<>();
        dueSince.forEach((verse, date) -> {
            if (Duration.between(date, now).compareTo(Duration.ofDays(dhorCycleDays)) > 0) {
                oldestStaleVerseByJuz.merge(
                        verse.juz(),
                        verse,
                        (left, right) -> dueSince.get(left).isBefore(dueSince.get(right)) ? left : right);
            }
        });
        List<StaleDhorPortion> stalePortions = oldestStaleVerseByJuz.values().stream()
                .map(verse -> new StaleDhorPortion(
                        verse.juz(),
                        lastReviewed.get(verse),
                        Duration.between(dueSince.get(verse), now).toDays()))
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
}
