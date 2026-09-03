package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.membership.Membership;
import com.houndjo.infrastructure.adapter.out.persistence.entity.MembershipEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link MembershipEntity} and {@link Membership}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MembershipMapper {

    default Membership toDomain(MembershipEntity entity) {
        if (entity == null) {
            return null;
        }
        return Membership.rehydrate(
                entity.getId(),
                entity.getUserId(),
                entity.getOrganizationId(),
                entity.getRole(),
                entity.getStatus(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    MembershipEntity toEntity(Membership domain);

    List<Membership> toDomain(List<MembershipEntity> entities);
}
