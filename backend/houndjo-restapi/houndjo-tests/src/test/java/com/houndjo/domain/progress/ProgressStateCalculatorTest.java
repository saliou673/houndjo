package com.houndjo.domain.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.progress.ProgressStateCalculator;
import com.houndjo.domain.models.progress.QuranPortionRef;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.domain.models.quran.VerseReference;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProgressStateCalculatorTest {
    private static final Instant NOW = Instant.parse("2026-09-05T12:00:00Z");
    private ProgressStateCalculator calculator;

    @BeforeEach
    void setUp() {
        QuranReferencePort reference = mock(QuranReferencePort.class);
        when(reference.versesBetween(any(), any())).thenAnswer(invocation -> {
            VerseReference from = invocation.getArgument(0);
            VerseReference to = invocation.getArgument(1);
            return IntStream.rangeClosed(from.verseNumber(), to.verseNumber())
                    .mapToObj(number -> new Verse(1, number, 1, 1, 1, 1))
                    .toList();
        });
        calculator = new ProgressStateCalculator(reference);
    }

    @Test
    void partialReviewMustNotClearOlderVersesInTheSameJuz() {
        var records = List.of(record(ProgressFlow.DHOR, 1, 7, 40), record(ProgressFlow.DHOR, 1, 3, 1));
        var state = calculator.compute(records, 1, 30, 30, NOW);
        assertThat(state.coveredJuz()).containsExactly(1);
        assertThat(state.stalePortions()).singleElement().satisfies(stale -> {
            assertThat(stale.daysSince()).isEqualTo(40);
            assertThat(stale.lastReviewedDate()).isEqualTo(NOW.minus(40, ChronoUnit.DAYS));
        });
        assertThat(calculator.compute(records, 1, 30, 42, NOW).stalePortions()).isEmpty();
    }

    @Test
    void unionOfRecentPartialReviewsClearsTheOldPortion() {
        var records = List.of(
                record(ProgressFlow.DHOR, 1, 3, 1),
                record(ProgressFlow.DHOR, 1, 7, 40),
                record(ProgressFlow.DHOR, 4, 7, 2));
        assertThat(calculator.compute(records, 1, 30, 30, NOW).stalePortions()).isEmpty();
    }

    @Test
    void memorizedVersesBecomeOverdueEvenWithoutAnyDhor() {
        var records = List.of(record(ProgressFlow.SABAK, 1, 7, 40), record(ProgressFlow.SABQI, 1, 7, 1));
        var state = calculator.compute(records, 1, 30, 30, NOW);
        assertThat(state.coveredJuz()).isEmpty();
        assertThat(state.stalePortions()).singleElement().satisfies(stale -> {
            assertThat(stale.lastReviewedDate()).isNull();
            assertThat(stale.daysSince()).isEqualTo(40);
        });
    }

    @Test
    void provisionalReviewDoesNotClearOverdueMemorization() {
        ProgressRecord provisional = record(ProgressFlow.DHOR, 1, 7, 1);
        provisional.update(provisional.getPortion(), 0, FluencyRating.GOOD, null, ProgressStatus.IN_PROGRESS, null);
        var records = List.of(record(ProgressFlow.SABAK, 1, 7, 40), provisional);
        assertThat(calculator.compute(records, 1, 30, 30, NOW).stalePortions()).hasSize(1);
        assertThat(calculator.compute(records, 2, 30, 30, NOW).stalePortions()).isEmpty();
    }

    @Test
    void thresholdIsExceededImmediatelyAfterTheConfiguredDuration() {
        var records = List.of(record(ProgressFlow.DHOR, 1, 7, 30));
        assertThat(calculator.compute(records, 1, 30, 30, NOW).stalePortions()).isEmpty();
        assertThat(calculator.compute(records, 1, 30, 30, NOW.plusSeconds(1)).stalePortions())
                .hasSize(1);
    }

    private ProgressRecord record(ProgressFlow flow, int fromVerse, int toVerse, int daysAgo) {
        Instant date = NOW.minus(daysAgo, ChronoUnit.DAYS);
        return ProgressRecord.rehydrate(
                1L,
                1L,
                1L,
                1L,
                1L,
                flow,
                new QuranPortionRef(1, fromVerse, 1, toVerse),
                0,
                FluencyRating.GOOD,
                null,
                ProgressStatus.VALIDATED,
                null,
                date,
                date,
                "test");
    }
}
