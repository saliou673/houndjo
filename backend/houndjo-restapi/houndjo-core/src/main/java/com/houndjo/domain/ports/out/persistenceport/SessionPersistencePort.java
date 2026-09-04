package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.session.Session;
import com.houndjo.domain.models.session.SessionFilter;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port for course sessions.
 */
public interface SessionPersistencePort {

    /**
     * Returns the sessions of a course within an organization matching the filter, paginated.
     *
     * @param courseId       the owning course identifier
     * @param organizationId the owning organization identifier
     * @param filter         search criteria
     * @param page           zero-based page index
     * @param size           maximum items per page
     * @return paginated sessions
     */
    PagedResult<Session> findByCourseIdAndOrganizationId(
            Long courseId, Long organizationId, SessionFilter filter, int page, int size);

    /**
     * Finds a session by its identifier within a course and organization.
     *
     * @param id             the session identifier
     * @param courseId       the owning course identifier
     * @param organizationId the owning organization identifier
     * @return the matching session, or empty if not found
     */
    Optional<Session> findByIdAndCourseIdAndOrganizationId(Long id, Long courseId, Long organizationId);

    /**
     * Persists or updates a session.
     *
     * @param session the session to save
     * @return the saved session
     */
    Session save(Session session);

    /**
     * Persists a batch of newly generated sessions.
     *
     * @param sessions the sessions to save
     * @return the saved sessions, in the same order
     */
    List<Session> saveAll(List<Session> sessions);
}
