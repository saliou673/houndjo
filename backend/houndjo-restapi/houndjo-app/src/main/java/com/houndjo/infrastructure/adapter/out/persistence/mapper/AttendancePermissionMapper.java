package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.attendance.AttendancePermission;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AttendancePermissionEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link AttendancePermissionEntity} and {@link AttendancePermission}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AttendancePermissionMapper {

    default AttendancePermission toDomain(AttendancePermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        return AttendancePermission.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getStudentId(),
                entity.getFromDate(),
                entity.getToDate(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    AttendancePermissionEntity toEntity(AttendancePermission domain);

    List<AttendancePermission> toDomain(List<AttendancePermissionEntity> entities);
}
