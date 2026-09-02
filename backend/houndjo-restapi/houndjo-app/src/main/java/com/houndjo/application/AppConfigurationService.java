package com.houndjo.application;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.domain.exceptions.AppConfigurationAlreadyExistsException;
import com.houndjo.domain.exceptions.AppConfigurationNotFoundException;
import com.houndjo.domain.models.appconfiguration.AppConfiguration;
import com.houndjo.domain.ports.in.AppConfigurationUseCase;
import com.houndjo.domain.ports.out.persistenceport.AppConfigurationPersistencePort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link AppConfigurationUseCase}: CRUD for configuration reference data.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AppConfigurationService implements AppConfigurationUseCase {

    private final AppConfigurationPersistencePort appConfigurationPersistencePort;

    @Override
    public AppConfiguration create(AppConfigurationCategory category, String code, String label, String description) {
        log.debug("Creating reference data: category={}, code={}", category, code);
        if (appConfigurationPersistencePort.existsByCategoryAndCode(category, code)) {
            throw new AppConfigurationAlreadyExistsException(category, code);
        }
        AppConfiguration appConfiguration = AppConfiguration.create(category, code, label, description);
        return appConfigurationPersistencePort.save(appConfiguration);
    }

    @Override
    public AppConfiguration update(Long id, String code, String label, String description, boolean active) {
        log.debug("Updating reference data id={}", id);
        AppConfiguration appConfiguration = appConfigurationPersistencePort
                .findById(id)
                .orElseThrow(() -> new AppConfigurationNotFoundException(id));

        if (appConfigurationPersistencePort.existsByCategoryAndCodeAndIdNot(appConfiguration.getCategory(), code, id)) {
            throw new AppConfigurationAlreadyExistsException(appConfiguration.getCategory(), code);
        }

        appConfiguration.update(code, label, description, active);
        return appConfigurationPersistencePort.save(appConfiguration);
    }

    @Override
    public AppConfiguration updateByCategoryAndCode(
            AppConfigurationCategory category,
            String code,
            String newCode,
            String label,
            String description,
            boolean active) {
        log.debug("Updating reference data: category={}, code={}", category, code);
        AppConfiguration appConfiguration = appConfigurationPersistencePort
                .findByCategoryAndCode(category, code)
                .orElseThrow(() -> new AppConfigurationNotFoundException(category, code));

        if (!code.equals(newCode)
                && appConfigurationPersistencePort.existsByCategoryAndCodeAndIdNot(
                        category, newCode, appConfiguration.getId())) {
            throw new AppConfigurationAlreadyExistsException(category, newCode);
        }

        appConfiguration.update(newCode, label, description, active);
        return appConfigurationPersistencePort.save(appConfiguration);
    }

    @Override
    public Optional<AppConfiguration> getByCategoryAndCode(AppConfigurationCategory category, String code) {
        return appConfigurationPersistencePort.findByCategoryAndCode(category, code);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting reference data id={}", id);
        AppConfiguration appConfiguration = appConfigurationPersistencePort
                .findById(id)
                .orElseThrow(() -> new AppConfigurationNotFoundException(id));
        appConfigurationPersistencePort.remove(appConfiguration);
    }

    @Override
    public AppConfiguration getById(Long id) {
        return appConfigurationPersistencePort
                .findById(id)
                .orElseThrow(() -> new AppConfigurationNotFoundException(id));
    }
}
