package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.PaceUnit;
import com.houndjo.domain.enumerations.QuranFlow;
import com.houndjo.domain.models.pace.CoursePace;
import com.houndjo.domain.models.pace.PaceFlow;
import com.houndjo.domain.models.quran.QuranPortion;
import java.math.BigDecimal;

/**
 * Use case for configuring a course's target pace and computing the next expected portion.
 */
public interface CoursePaceUseCase {

    /**
     * Creates or replaces the pace configuration of a course. For {@code QURAN} courses, all
     * three flows (Sabak, Sabqi, Dhor) plus {@code dhorCycleDays} are required.
     */
    CoursePace setPace(
            Long courseId,
            PaceUnit unit,
            BigDecimal amountPerSession,
            int sessionsPerWeek,
            PaceFlow sabak,
            PaceFlow sabqi,
            PaceFlow dhor,
            Integer dhorCycleDays);

    /**
     * Returns the pace configuration of a course.
     *
     * @param courseId the course identifier
     * @return the course's pace
     */
    CoursePace getPace(Long courseId);

    /**
     * Computes the target portion for a student's next session on a given flow, starting from
     * the beginning of the course's Quran scope (progress tracking, when it exists, will refine
     * this to the student's actual current position).
     *
     * @param courseId  the course identifier (must be a {@code QURAN} course)
     * @param studentId the student identifier
     * @param flow      the flow to compute the portion for
     * @return the target portion
     */
    QuranPortion getNextPortion(Long courseId, Long studentId, QuranFlow flow);
}
