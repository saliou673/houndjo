package com.houndjo.domain.models.progress;

import java.util.List;

/**
 * One (student, course) pair with at least one stale Dhor portion, for the organization-wide
 * revision-alerts dashboard aggregate.
 *
 * @param studentId     the student identifier
 * @param courseId      the owning QURAN course identifier
 * @param stalePortions the student's stale Dhor portions for that course
 */
public record RevisionAlert(Long studentId, Long courseId, List<StaleDhorPortion> stalePortions) {}
