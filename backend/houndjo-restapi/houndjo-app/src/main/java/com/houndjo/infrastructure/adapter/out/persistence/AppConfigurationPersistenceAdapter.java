package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.domain.models.appconfiguration.AppConfiguration;
import com.houndjo.domain.ports.out.persistenceport.AppConfigurationPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.AppConfigurationMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.AppConfigurationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link AppConfigurationPersistencePort}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AppConfigurationPersistenceAdapter implements AppConfigurationPersistencePort {

    private final AppConfigurationRepository appConfigurationRepository;
    private final AppConfigurationMapper appConfigurationMapper;

    @Override
    public AppConfiguration save(AppConfiguration appConfiguration) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> appConfigurationMapper.toDomain(
                        appConfigurationRepository.save(appConfigurationMapper.toEntity(appConfiguration))),
                "Error saving reference data with code: " + appConfiguration.getCode());
    }

    @Override
    public Optional<AppConfiguration> findById(Long id) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> appConfigurationRepository.findById(id).map(appConfigurationMapper::toDomain),
                "Error fetching reference data by id: " + id);
    }

    @Override
    public boolean existsByCategoryAndCode(AppConfigurationCategory category, String code) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> appConfigurationRepository.existsByCategoryAndCode(category, code),
                "Error checking reference data existence for category " + category + " and code: " + code);
    }

    @Override
    public boolean existsByCategoryAndCodeAndIdNot(AppConfigurationCategory category, String code, Long excludeId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> appConfigurationRepository.existsByCategoryAndCodeAndIdNot(category, code, excludeId),
                "Error checking reference data existence for category " + category + " and code: " + code);
    }

    @Override
    public boolean existsActiveByCategoryAndCode(AppConfigurationCategory category, String code) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> appConfigurationRepository.existsByCategoryAndCodeAndActiveTrue(category, code),
                "Error checking active reference data existence for category " + category + " and code: " + code);
    }

    @Override
    public Optional<AppConfiguration> findByCategoryAndCode(AppConfigurationCategory category, String code) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> appConfigurationRepository
                        .findByCategoryAndCode(category, code)
                        .map(appConfigurationMapper::toDomain),
                "Error fetching reference data for category " + category + " and code: " + code);
    }

    @Override
    public void remove(AppConfiguration appConfiguration) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> appConfigurationRepository.delete(appConfigurationMapper.toEntity(appConfiguration)),
                "Error removing reference data with id: " + appConfiguration.getId());
    }
}
