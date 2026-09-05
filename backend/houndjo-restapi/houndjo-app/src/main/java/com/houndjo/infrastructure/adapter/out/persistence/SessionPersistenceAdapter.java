package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.session.Session;
import com.houndjo.domain.models.session.SessionFilter;
import com.houndjo.domain.ports.out.persistenceport.SessionPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.SessionEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.SessionMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.SessionRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link SessionPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class SessionPersistenceAdapter implements SessionPersistencePort {

    // Wide-open sentinel bounds standing in for "no constraint" on the corresponding side of the
    // date range, well within PostgreSQL's DATE range.
    private static final LocalDate MIN_DATE = LocalDate.of(1, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    @Override
    public List<Session> findByIdsAndOrganizationId(Collection<Long> ids, Long organizationId) {
        if (ids.isEmpty()) return List.of();
        return AdapterPersistenceUtils.executeDbOperation(
                () -> sessionMapper.toDomain(sessionRepository.findByIdInAndOrganizationId(ids, organizationId)),
                "Error fetching sessions by ids");
    }

    @Override
    public PagedResult<Session> findByCourseIdAndOrganizationId(
            Long courseId, Long organizationId, SessionFilter filter, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.asc("sessionDate")));
                    LocalDate fromDate = filter == null || filter.fromDate() == null ? MIN_DATE : filter.fromDate();
                    LocalDate toDate = filter == null || filter.toDate() == null ? MAX_DATE : filter.toDate();
                    Page<SessionEntity> entityPage =
                            sessionRepository.search(courseId, organizationId, fromDate, toDate, pageRequest);
                    List<Session> items = sessionMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated sessions");
    }

    @Override
    public Optional<Session> findByIdAndCourseIdAndOrganizationId(Long id, Long courseId, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> sessionRepository
                        .findByIdAndCourseIdAndOrganizationId(id, courseId, organizationId)
                        .map(sessionMapper::toDomain),
                "Error fetching session by id");
    }

    @Override
    public Optional<Session> findByIdAndOrganizationId(Long id, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> sessionRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .map(sessionMapper::toDomain),
                "Error fetching session by id");
    }

    @Override
    @Transactional
    public Session save(Session session) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> sessionMapper.toDomain(sessionRepository.save(sessionMapper.toEntity(session))),
                "Error saving session");
    }

    @Override
    @Transactional
    public List<Session> saveAll(List<Session> sessions) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> sessionMapper.toDomain(sessionRepository.saveAll(sessionMapper.toEntity(sessions))),
                "Error saving sessions");
    }
}
