package com.houndjo.domain.models.progress;

import java.util.List;

/**
 * A student's actual state across the three Quran flows for one course, computed from their
 * validated {@link ProgressRecord} history: Sabak/Sabqi positions, Dhor coverage, and the juz
 * whose Dhor revision has gone stale (past the course's {@code dhorCycleDays} threshold) —
 * the core differentiator over tracking Sabak alone.
 *
 * @param sabak         the last validated Sabak portion, or {@code null} if none recorded yet
 * @param sabqi         the last validated Sabqi portion, or {@code null} if none recorded yet
 * @param coveredJuz    every juz Dhor-revised at least once, ascending
 * @param stalePortions juz containing overdue verses, including memorized verses never Dhor-revised
 */
public record ProgressState(
        FlowSnapshot sabak, FlowSnapshot sabqi, List<Integer> coveredJuz, List<StaleDhorPortion> stalePortions) {}
