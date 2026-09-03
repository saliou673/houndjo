package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.organization.Organization;
import com.houndjo.infrastructure.adapter.out.persistence.entity.OrganizationEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link OrganizationEntity} and {@link Organization}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrganizationMapper {

    default Organization toDomain(OrganizationEntity entity) {
        if (entity == null) {
            return null;
        }
        return Organization.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getContactEmail(),
                entity.getPhoneNumber(),
                entity.getAddress(),
                entity.getDefaultCurrencyCode(),
                entity.getDefaultLanguageKey(),
                entity.getTimezone(),
                entity.getStatus(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    OrganizationEntity toEntity(Organization domain);

    List<Organization> toDomain(List<OrganizationEntity> entities);
}
