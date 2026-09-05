package com.houndjo.domain.models.progress;

import java.time.Instant;

/**
 * A juz containing a verse whose Dhor revision exceeds the course's threshold.
 *
 * @param juz              the stale juz, 1..30
 * @param lastReviewedDate last Dhor review of the most overdue verse, or null if never reviewed
 * @param daysSince        days since that review, or first memorization when never reviewed
 */
public record StaleDhorPortion(int juz, Instant lastReviewedDate, long daysSince) {}
