package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.exceptions.CourseNotFoundException;
import com.houndjo.domain.exceptions.CoursePaceNotFoundException;
import com.houndjo.domain.exceptions.SessionNotFoundException;
import com.houndjo.domain.models.pace.CoursePace;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.session.Session;
import com.houndjo.domain.models.session.SessionFilter;
import com.houndjo.domain.ports.in.SessionUseCase;
import com.houndjo.domain.ports.out.persistenceport.CoursePacePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.CoursePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.SessionPersistencePort;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link SessionUseCase}: single and recurring session
 * management for courses of the active organization.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SessionService implements SessionUseCase {

    private final SessionPersistencePort sessionPersistencePort;
    private final CoursePersistencePort coursePersistencePort;
    private final CoursePacePersistencePort coursePacePersistencePort;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Session> findAll(Long courseId, SessionFilter filter, int page, int size) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireCourse(courseId, organizationId);
        return sessionPersistencePort.findByCourseIdAndOrganizationId(courseId, organizationId, filter, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Session getById(Long courseId, Long id) {
        return getByIdOrThrow(courseId, id);
    }

    @Override
    public Session create(
            Long courseId, LocalDate sessionDate, LocalTime startTime, LocalTime endTime, Long teacherUserId) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireCourse(courseId, organizationId);
        log.debug(
                "Creating session: organizationId={} courseId={} sessionDate={}",
                organizationId,
                courseId,
                sessionDate);
        Session session = Session.create(organizationId, courseId, sessionDate, startTime, endTime, teacherUserId);
        return sessionPersistencePort.save(session);
    }

    @Override
    public List<Session> generate(Long courseId, LocalDate fromDate, LocalDate toDate) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireCourse(courseId, organizationId);
        CoursePace pace = coursePacePersistencePort
                .findByCourseIdAndOrganizationId(courseId, organizationId)
                .orElseThrow(() -> new CoursePaceNotFoundException(courseId));
        log.debug(
                "Generating sessions: organizationId={} courseId={} fromDate={} toDate={}",
                organizationId,
                courseId,
                fromDate,
                toDate);
        List<Session> sessions =
                Session.generateFor(organizationId, courseId, pace.getSessionsPerWeek(), fromDate, toDate);
        return sessionPersistencePort.saveAll(sessions);
    }

    @Override
    public Session update(
            Long courseId, Long id, LocalDate sessionDate, LocalTime startTime, LocalTime endTime, Long teacherUserId) {
        log.debug("Updating session id={}", id);
        Session session = getByIdOrThrow(courseId, id);
        session.update(sessionDate, startTime, endTime, teacherUserId);
        return sessionPersistencePort.save(session);
    }

    @Override
    public Session cancel(Long courseId, Long id) {
        log.debug("Cancelling session id={}", id);
        Session session = getByIdOrThrow(courseId, id);
        session.cancel();
        return sessionPersistencePort.save(session);
    }

    private Session getByIdOrThrow(Long courseId, Long id) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireCourse(courseId, organizationId);
        return sessionPersistencePort
                .findByIdAndCourseIdAndOrganizationId(id, courseId, organizationId)
                .orElseThrow(() -> new SessionNotFoundException(id));
    }

    private void requireCourse(Long courseId, Long organizationId) {
        coursePersistencePort
                .findByIdAndOrganizationId(courseId, organizationId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }
}
