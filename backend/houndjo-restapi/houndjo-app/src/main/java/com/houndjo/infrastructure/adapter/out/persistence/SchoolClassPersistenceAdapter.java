package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.out.persistenceport.SchoolClassPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.SchoolClassEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.SchoolClassMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.SchoolClassRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link SchoolClassPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class SchoolClassPersistenceAdapter implements SchoolClassPersistencePort {

    private final SchoolClassRepository schoolClassRepository;
    private final SchoolClassMapper schoolClassMapper;

    @Override
    public PagedResult<SchoolClass> findByOrganizationId(Long organizationId, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    Page<SchoolClassEntity> entityPage = schoolClassRepository.findByOrganizationId(
                            organizationId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate")));
                    List<SchoolClass> items = schoolClassMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated classes");
    }

    @Override
    public Optional<SchoolClass> findByIdAndOrganizationId(Long id, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> schoolClassRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .map(schoolClassMapper::toDomain),
                "Error fetching class by id");
    }

    @Override
    @Transactional
    public SchoolClass save(SchoolClass schoolClass) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> schoolClassMapper.toDomain(schoolClassRepository.save(schoolClassMapper.toEntity(schoolClass))),
                "Error saving class");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AdapterPersistenceUtils.executeDbOperation(() -> schoolClassRepository.deleteById(id), "Error deleting class");
    }
}
