package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.enrollment.Enrollment;
import com.houndjo.infrastructure.adapter.out.persistence.entity.EnrollmentEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link EnrollmentEntity} and {@link Enrollment}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EnrollmentMapper {

    default Enrollment toDomain(EnrollmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return Enrollment.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getStudentId(),
                entity.getClassId(),
                entity.getCourseIds(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    EnrollmentEntity toEntity(Enrollment domain);

    List<Enrollment> toDomain(List<EnrollmentEntity> entities);
}
