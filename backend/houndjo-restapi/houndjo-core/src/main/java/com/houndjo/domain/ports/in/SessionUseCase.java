package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.session.Session;
import com.houndjo.domain.models.session.SessionFilter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Use case for managing a course's sessions within the active organization.
 */
public interface SessionUseCase {

    /**
     * Returns the sessions of a course matching the filter, paginated.
     *
     * @param courseId the owning course identifier
     * @param filter   search criteria
     * @param page     zero-based page index
     * @param size     maximum items per page
     * @return paginated sessions
     */
    PagedResult<Session> findAll(Long courseId, SessionFilter filter, int page, int size);

    /**
     * Returns a session by its identifier within a course.
     *
     * @param courseId the owning course identifier
     * @param id       the session identifier
     * @return the matching session
     */
    Session getById(Long courseId, Long id);

    /**
     * Creates a single session for a course.
     */
    Session create(Long courseId, LocalDate sessionDate, LocalTime startTime, LocalTime endTime, Long teacherUserId);

    /**
     * Generates recurring {@code PLANNED} sessions for a course between {@code fromDate} and
     * {@code toDate}, at the course's configured {@code sessionsPerWeek} cadence.
     *
     * @param courseId the owning course identifier
     * @param fromDate first day of the generation range, inclusive
     * @param toDate   last day of the generation range, inclusive
     * @return the generated sessions, in date order
     */
    List<Session> generate(Long courseId, LocalDate fromDate, LocalDate toDate);

    /**
     * Updates an existing session of a course.
     */
    Session update(
            Long courseId, Long id, LocalDate sessionDate, LocalTime startTime, LocalTime endTime, Long teacherUserId);

    /**
     * Cancels a session of a course.
     *
     * @param courseId the owning course identifier
     * @param id       the session identifier
     * @return the cancelled session
     */
    Session cancel(Long courseId, Long id);
}
