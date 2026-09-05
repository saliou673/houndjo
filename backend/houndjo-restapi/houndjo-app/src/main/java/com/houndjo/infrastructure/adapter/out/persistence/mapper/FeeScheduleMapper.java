package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.billing.FeeSchedule;
import com.houndjo.infrastructure.adapter.out.persistence.entity.FeeScheduleEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link FeeScheduleEntity} and {@link FeeSchedule}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FeeScheduleMapper {

    default FeeSchedule toDomain(FeeScheduleEntity entity) {
        if (entity == null) {
            return null;
        }
        return FeeSchedule.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getType(),
                entity.getAmount(),
                entity.getCurrencyCode(),
                entity.getLabel(),
                entity.isActive(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    FeeScheduleEntity toEntity(FeeSchedule domain);

    List<FeeSchedule> toDomain(List<FeeScheduleEntity> entities);
}
