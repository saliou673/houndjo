package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.session.Session;
import com.houndjo.infrastructure.adapter.out.persistence.entity.SessionEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link SessionEntity} and {@link Session}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SessionMapper {

    default Session toDomain(SessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return Session.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getCourseId(),
                entity.getTeacherUserId(),
                entity.getSessionDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    SessionEntity toEntity(Session domain);

    List<Session> toDomain(List<SessionEntity> entities);

    List<SessionEntity> toEntity(List<Session> domain);
}
