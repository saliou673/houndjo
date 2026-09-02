package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.appconfiguration.AppConfiguration;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AppConfigurationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link com.houndjo.infrastructure.adapter.out.persistence.entity.AppConfigurationEntity} and {@link com.houndjo.domain.models.appconfiguration.AppConfiguration}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AppConfigurationMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    AppConfigurationEntity toEntity(AppConfiguration appConfiguration);

    default AppConfiguration toDomain(AppConfigurationEntity entity) {
        if (entity == null) {
            return null;
        }
        return AppConfiguration.rehydrate(
                entity.getId(),
                entity.getCategory(),
                entity.getCode(),
                entity.getLabel(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }
}
