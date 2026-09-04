package com.houndjo.domain.models.session;

import com.houndjo.domain.enumerations.SessionStatus;
import com.houndjo.domain.models.Auditable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate representing a single class session for a course.
 */
@Getter
public class Session extends Auditable<Long> {

    private final Long organizationId;
    private final Long courseId;
    private Long teacherUserId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionStatus status;

    private Session(
            Long id,
            Long organizationId,
            Long courseId,
            Long teacherUserId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            SessionStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.courseId = courseId;
        this.teacherUserId = teacherUserId;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public static Session create(
            Long organizationId,
            Long courseId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            Long teacherUserId) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(courseId, "courseId must not be null");
        Objects.requireNonNull(sessionDate, "sessionDate must not be null");
        return new Session(
                null,
                organizationId,
                courseId,
                teacherUserId,
                sessionDate,
                startTime,
                endTime,
                SessionStatus.PLANNED,
                null,
                null,
                null);
    }

    public static Session rehydrate(
            Long id,
            Long organizationId,
            Long courseId,
            Long teacherUserId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            SessionStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new Session(
                id,
                organizationId,
                courseId,
                teacherUserId,
                sessionDate,
                startTime,
                endTime,
                status,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    /**
     * Generates {@code PLANNED} sessions for a course between {@code fromDate} and {@code toDate}
     * (inclusive), at {@code sessionsPerWeek} occurrences per week. The weekly weekdays are
     * chosen deterministically, spread as evenly as possible starting on Monday (e.g. 3/week →
     * Monday, Wednesday, Friday) — this MVP has no separate per-course weekly schedule to draw
     * from yet.
     *
     * @param organizationId  the owning organization identifier
     * @param courseId        the course identifier
     * @param sessionsPerWeek weekly session cadence, 1..7
     * @param fromDate        first day of the generation range, inclusive
     * @param toDate          last day of the generation range, inclusive
     * @return the generated sessions, in date order
     */
    public static List<Session> generateFor(
            Long organizationId, Long courseId, int sessionsPerWeek, LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(courseId, "courseId must not be null");
        Objects.requireNonNull(fromDate, "fromDate must not be null");
        Objects.requireNonNull(toDate, "toDate must not be null");
        if (sessionsPerWeek < 1 || sessionsPerWeek > 7) {
            throw new IllegalArgumentException("sessionsPerWeek must be between 1 and 7");
        }
        List<DayOfWeek> scheduledDays = scheduledWeekdays(sessionsPerWeek);
        List<Session> sessions = new ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            if (scheduledDays.contains(date.getDayOfWeek())) {
                sessions.add(create(organizationId, courseId, date, null, null, null));
            }
        }
        return sessions;
    }

    private static List<DayOfWeek> scheduledWeekdays(int sessionsPerWeek) {
        List<DayOfWeek> days = new ArrayList<>();
        DayOfWeek[] week = DayOfWeek.values();
        for (int i = 0; i < sessionsPerWeek; i++) {
            days.add(week[(i * 7) / sessionsPerWeek]);
        }
        return days;
    }

    public void update(LocalDate sessionDate, LocalTime startTime, LocalTime endTime, Long teacherUserId) {
        Objects.requireNonNull(sessionDate, "sessionDate must not be null");
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.teacherUserId = teacherUserId;
    }

    public void cancel() {
        this.status = SessionStatus.CANCELLED;
    }
}
