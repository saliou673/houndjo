package com.houndjo.domain.models.progress;

import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import com.houndjo.domain.exceptions.InvalidProgressPortionException;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate recording the portion worked plus an assessment for a (student, session) pair, on
 * one of the three independent Quran flows (Sabak/Sabqi/Dhor) or the QAIDA/BOOK lesson/chapter
 * flows. A student can have one independent record per flow for the same session.
 */
@Getter
public class ProgressRecord extends Auditable<Long> {

    private final Long organizationId;
    private final Long studentId;
    private final Long courseId;
    private final Long sessionId;
    private final ProgressFlow flow;
    private PortionRef portion;
    private int errorCount;
    private FluencyRating fluency;
    private FluencyRating tajweed;
    private ProgressStatus status;
    private String note;

    private ProgressRecord(
            Long id,
            Long organizationId,
            Long studentId,
            Long courseId,
            Long sessionId,
            ProgressFlow flow,
            PortionRef portion,
            int errorCount,
            FluencyRating fluency,
            FluencyRating tajweed,
            ProgressStatus status,
            String note,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.sessionId = sessionId;
        this.flow = flow;
        this.portion = portion;
        this.errorCount = errorCount;
        this.fluency = fluency;
        this.tajweed = tajweed;
        this.status = status;
        this.note = note;
    }

    public static ProgressRecord create(
            Long organizationId,
            Long studentId,
            Long courseId,
            Long sessionId,
            ProgressFlow flow,
            PortionRef portion,
            int errorCount,
            FluencyRating fluency,
            FluencyRating tajweed,
            ProgressStatus status,
            String note) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(studentId, "studentId must not be null");
        Objects.requireNonNull(courseId, "courseId must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(flow, "flow must not be null");
        Objects.requireNonNull(portion, "portion must not be null");
        Objects.requireNonNull(fluency, "fluency must not be null");
        Objects.requireNonNull(status, "status must not be null");
        requireMatchingPortion(flow, portion);
        requirePositiveOrZero(errorCount);
        return new ProgressRecord(
                null,
                organizationId,
                studentId,
                courseId,
                sessionId,
                flow,
                portion,
                errorCount,
                fluency,
                tajweed,
                status,
                note,
                null,
                null,
                null);
    }

    public static ProgressRecord rehydrate(
            Long id,
            Long organizationId,
            Long studentId,
            Long courseId,
            Long sessionId,
            ProgressFlow flow,
            PortionRef portion,
            int errorCount,
            FluencyRating fluency,
            FluencyRating tajweed,
            ProgressStatus status,
            String note,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new ProgressRecord(
                id,
                organizationId,
                studentId,
                courseId,
                sessionId,
                flow,
                portion,
                errorCount,
                fluency,
                tajweed,
                status,
                note,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    public void update(
            PortionRef portion,
            int errorCount,
            FluencyRating fluency,
            FluencyRating tajweed,
            ProgressStatus status,
            String note) {
        Objects.requireNonNull(portion, "portion must not be null");
        Objects.requireNonNull(fluency, "fluency must not be null");
        Objects.requireNonNull(status, "status must not be null");
        requireMatchingPortion(flow, portion);
        requirePositiveOrZero(errorCount);
        this.portion = portion;
        this.errorCount = errorCount;
        this.fluency = fluency;
        this.tajweed = tajweed;
        this.status = status;
        this.note = note;
    }

    private static void requireMatchingPortion(ProgressFlow flow, PortionRef portion) {
        boolean matches =
                switch (flow) {
                    case SABAK, SABQI, DHOR -> portion instanceof QuranPortionRef;
                    case LESSON -> portion instanceof LessonPortionRef;
                    case CHAPTER -> portion instanceof ChapterPortionRef;
                };
        if (!matches) {
            throw new InvalidProgressPortionException(flow);
        }
    }

    private static void requirePositiveOrZero(int errorCount) {
        if (errorCount < 0) {
            throw new IllegalArgumentException("errorCount must be >= 0");
        }
    }
}
