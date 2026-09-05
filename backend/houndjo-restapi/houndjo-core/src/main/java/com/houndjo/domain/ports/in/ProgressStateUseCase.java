package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.progress.ProgressState;
import com.houndjo.domain.models.progress.RevisionAlert;
import java.util.List;

/**
 * Use case computing a student's actual state across the three Quran flows (Sabak/Sabqi/Dhor)
 * and detecting stale (overdue) Dhor revisions — the core differentiator over tracking Sabak
 * alone.
 */
public interface ProgressStateUseCase {

    /**
     * Computes a student's progress state for a {@code QURAN} course of the active organization.
     *
     * @param studentId the student identifier
     * @param courseId  the course identifier (must be a {@code QURAN} course with a configured
     *                  pace)
     * @return the computed progress state
     */
    ProgressState getProgressState(Long studentId, Long courseId);

    /**
     * Returns every (student, course) pair of an organization with at least one stale Dhor
     * portion, for the dashboard's revision-alerts aggregate.
     *
     * @param organizationId the organization identifier (must be the active organization)
     * @return the students with overdue Dhor revisions
     */
    List<RevisionAlert> getRevisionAlerts(Long organizationId);
}
