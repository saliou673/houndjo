package com.houndjo.infrastructure.adapter.out.query;

import com.houndjo.domain.models.appconfiguration.AppConfiguration;
import com.houndjo.domain.models.appconfiguration.AppConfigurationFilter;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.AppConfigurationQueryUseCase;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AppConfigurationEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AppConfigurationEntity_;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.AppConfigurationMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.AppConfigurationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query service implementing {@link AppConfigurationQueryUseCase} with JPA Specification-based filtering.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppConfigurationQueryService extends QueryService<AppConfigurationEntity>
        implements AppConfigurationQueryUseCase {

    private final AppConfigurationRepository appConfigurationRepository;
    private final AppConfigurationMapper appConfigurationMapper;

    @Override
    public PagedResult<AppConfiguration> findAll(AppConfigurationFilter filter, int page, int size) {
        log.debug("Finding reference data by filter: {}", filter);
        Page<AppConfigurationEntity> entityPage = appConfigurationRepository.findAll(
                createSpecification(filter), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate")));
        List<AppConfiguration> items = entityPage.getContent().stream()
                .map(appConfigurationMapper::toDomain)
                .toList();
        return new PagedResult<>(items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
    }

    @Override
    public long count(AppConfigurationFilter filter) {
        log.debug("Counting reference data by filter: {}", filter);
        return appConfigurationRepository.count(createSpecification(filter));
    }

    private Specification<AppConfigurationEntity> createSpecification(AppConfigurationFilter filter) {
        Specification<AppConfigurationEntity> spec = Specification.unrestricted();

        if (filter == null) {
            return spec;
        }

        if (filter.getId() != null) {
            spec = spec.and(buildRangeSpecification(filter.getId(), AppConfigurationEntity_.id));
        }

        if (filter.getCategory() != null) {
            spec = spec.and(buildEnumSpecification(filter.getCategory(), AppConfigurationEntity_.category));
        }

        if (filter.getCode() != null) {
            spec = spec.and(buildStringSpecification(filter.getCode(), AppConfigurationEntity_.code));
        }

        if (filter.getLabel() != null) {
            spec = spec.and(buildStringSpecification(filter.getLabel(), AppConfigurationEntity_.label));
        }

        if (filter.getActive() != null) {
            spec = spec.and(buildSpecification(filter.getActive(), AppConfigurationEntity_.active));
        }

        spec = addAuditFieldsSpecifications(
                spec,
                filter,
                AppConfigurationEntity_.creationDate,
                AppConfigurationEntity_.lastUpdateDate,
                AppConfigurationEntity_.lastUpdatedBy);

        return spec;
    }
}
