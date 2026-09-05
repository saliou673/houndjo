package com.houndjo.domain.models.progress;

import java.time.Instant;

/**
 * A juz that has been Dhor-revised at least once, but whose last revision now exceeds the
 * course's {@code dhorCycleDays} threshold.
 *
 * @param juz              the stale juz, 1..30
 * @param lastReviewedDate when the juz was last Dhor-revised
 * @param daysSince        days elapsed since {@code lastReviewedDate}
 */
public record StaleDhorPortion(int juz, Instant lastReviewedDate, long daysSince) {}
