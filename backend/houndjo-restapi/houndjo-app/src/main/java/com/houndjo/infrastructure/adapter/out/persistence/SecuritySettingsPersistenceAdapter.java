package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.securitysettings.SecuritySettings;
import com.houndjo.domain.ports.out.persistenceport.SecuritySettingsPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.SecuritySettingsMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.SecuritySettingsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link SecuritySettingsPersistencePort}.
 * Uses findFirst to retrieve the singleton row.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SecuritySettingsPersistenceAdapter implements SecuritySettingsPersistencePort {

    private final SecuritySettingsRepository securitySettingsRepository;
    private final SecuritySettingsMapper securitySettingsMapper;

    @Override
    public SecuritySettings save(SecuritySettings securitySettings) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> securitySettingsMapper.toDomain(
                        securitySettingsRepository.save(securitySettingsMapper.toEntity(securitySettings))),
                "Error saving security settings");
    }

    @Override
    public Optional<SecuritySettings> find() {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> securitySettingsRepository.findAll().stream().findFirst().map(securitySettingsMapper::toDomain),
                "Error fetching security settings");
    }
}
