package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.infrastructure.adapter.out.persistence.entity.SchoolClassEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link SchoolClassEntity} and {@link SchoolClass}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SchoolClassMapper {

    default SchoolClass toDomain(SchoolClassEntity entity) {
        if (entity == null) {
            return null;
        }
        return SchoolClass.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDisplayOrder(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    SchoolClassEntity toEntity(SchoolClass domain);

    List<SchoolClass> toDomain(List<SchoolClassEntity> entities);
}
