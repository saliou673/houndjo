package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AttendanceEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link AttendanceEntity} and {@link Attendance}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AttendanceMapper {

    default Attendance toDomain(AttendanceEntity entity) {
        if (entity == null) {
            return null;
        }
        return Attendance.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getStudentId(),
                entity.getSessionId(),
                entity.getStatus(),
                entity.getReason(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    AttendanceEntity toEntity(Attendance domain);

    List<Attendance> toDomain(List<AttendanceEntity> entities);

    List<AttendanceEntity> toEntity(List<Attendance> domain);
}
