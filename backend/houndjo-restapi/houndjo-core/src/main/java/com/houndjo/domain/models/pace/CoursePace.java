package com.houndjo.domain.models.pace;

import com.houndjo.domain.enumerations.PaceUnit;
import com.houndjo.domain.models.Auditable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Target pace configuration for a course. {@code unit}/{@code amountPerSession} always apply;
 * {@code sabak}/{@code sabqi}/{@code dhor} (and {@code dhorCycleDays}) are populated only for
 * {@code QURAN} courses, where the three flows are tracked and paced independently.
 */
@Getter
public class CoursePace extends Auditable<Long> {

    private final Long organizationId;
    private final Long courseId;
    private PaceUnit unit;
    private BigDecimal amountPerSession;
    private int sessionsPerWeek;
    private PaceFlow sabak;
    private PaceFlow sabqi;
    private PaceFlow dhor;
    private Integer dhorCycleDays;

    private CoursePace(
            Long id,
            Long organizationId,
            Long courseId,
            PaceUnit unit,
            BigDecimal amountPerSession,
            int sessionsPerWeek,
            PaceFlow sabak,
            PaceFlow sabqi,
            PaceFlow dhor,
            Integer dhorCycleDays,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.courseId = courseId;
        this.unit = unit;
        this.amountPerSession = amountPerSession;
        this.sessionsPerWeek = sessionsPerWeek;
        this.sabak = sabak;
        this.sabqi = sabqi;
        this.dhor = dhor;
        this.dhorCycleDays = dhorCycleDays;
    }

    public static CoursePace create(
            Long organizationId,
            Long courseId,
            PaceUnit unit,
            BigDecimal amountPerSession,
            int sessionsPerWeek,
            PaceFlow sabak,
            PaceFlow sabqi,
            PaceFlow dhor,
            Integer dhorCycleDays) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(courseId, "courseId must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        Objects.requireNonNull(amountPerSession, "amountPerSession must not be null");
        return new CoursePace(
                null,
                organizationId,
                courseId,
                unit,
                amountPerSession,
                sessionsPerWeek,
                sabak,
                sabqi,
                dhor,
                dhorCycleDays,
                null,
                null,
                null);
    }

    public static CoursePace rehydrate(
            Long id,
            Long organizationId,
            Long courseId,
            PaceUnit unit,
            BigDecimal amountPerSession,
            int sessionsPerWeek,
            PaceFlow sabak,
            PaceFlow sabqi,
            PaceFlow dhor,
            Integer dhorCycleDays,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new CoursePace(
                id,
                organizationId,
                courseId,
                unit,
                amountPerSession,
                sessionsPerWeek,
                sabak,
                sabqi,
                dhor,
                dhorCycleDays,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    public void update(
            PaceUnit unit,
            BigDecimal amountPerSession,
            int sessionsPerWeek,
            PaceFlow sabak,
            PaceFlow sabqi,
            PaceFlow dhor,
            Integer dhorCycleDays) {
        Objects.requireNonNull(unit, "unit must not be null");
        Objects.requireNonNull(amountPerSession, "amountPerSession must not be null");
        this.unit = unit;
        this.amountPerSession = amountPerSession;
        this.sessionsPerWeek = sessionsPerWeek;
        this.sabak = sabak;
        this.sabqi = sabqi;
        this.dhor = dhor;
        this.dhorCycleDays = dhorCycleDays;
    }
}
