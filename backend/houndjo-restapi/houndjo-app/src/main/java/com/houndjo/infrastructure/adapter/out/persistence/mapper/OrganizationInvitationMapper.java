package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.organization.OrganizationInvitation;
import com.houndjo.infrastructure.adapter.out.persistence.entity.OrganizationInvitationEntity;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrganizationInvitationMapper {
    default OrganizationInvitation toDomain(OrganizationInvitationEntity e) {
        if (e == null) return null;
        return OrganizationInvitation.rehydrate(
                e.getId(),
                e.getOrganizationId(),
                e.getEmail(),
                e.getRole(),
                e.getInvitationCode(),
                e.getExpiresAt(),
                e.getStatus(),
                e.getCreationDate(),
                e.getLastUpdateDate(),
                e.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    OrganizationInvitationEntity toEntity(OrganizationInvitation domain);

    List<OrganizationInvitation> toDomain(List<OrganizationInvitationEntity> entities);
}
