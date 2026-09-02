package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.securitysettings.SecuritySettings;
import com.houndjo.infrastructure.adapter.out.persistence.entity.SecuritySettingsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link SecuritySettingsEntity} and {@link SecuritySettings}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SecuritySettingsMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    SecuritySettingsEntity toEntity(SecuritySettings securitySettings);

    default SecuritySettings toDomain(SecuritySettingsEntity entity) {
        if (entity == null) {
            return null;
        }
        return SecuritySettings.rehydrate(
                entity.getId(),
                entity.isTwoFactorRequired(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }
}
