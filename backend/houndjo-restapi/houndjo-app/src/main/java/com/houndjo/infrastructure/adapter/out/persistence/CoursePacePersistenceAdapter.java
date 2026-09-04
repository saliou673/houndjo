package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.pace.CoursePace;
import com.houndjo.domain.ports.out.persistenceport.CoursePacePersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.CoursePaceMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.CoursePaceRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link CoursePacePersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class CoursePacePersistenceAdapter implements CoursePacePersistencePort {

    private final CoursePaceRepository coursePaceRepository;
    private final CoursePaceMapper coursePaceMapper;

    @Override
    public Optional<CoursePace> findByCourseIdAndOrganizationId(Long courseId, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> coursePaceRepository
                        .findByCourseIdAndOrganizationId(courseId, organizationId)
                        .map(coursePaceMapper::toDomain),
                "Error fetching course pace");
    }

    @Override
    @Transactional
    public CoursePace save(CoursePace coursePace) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> coursePaceMapper.toDomain(coursePaceRepository.save(coursePaceMapper.toEntity(coursePace))),
                "Error saving course pace");
    }
}
