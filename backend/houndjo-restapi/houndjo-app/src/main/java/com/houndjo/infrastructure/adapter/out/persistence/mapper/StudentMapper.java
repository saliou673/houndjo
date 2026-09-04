package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.student.Student;
import com.houndjo.infrastructure.adapter.out.persistence.entity.StudentEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link StudentEntity} and {@link Student}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StudentMapper {

    default Student toDomain(StudentEntity entity) {
        if (entity == null) {
            return null;
        }
        return Student.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getBirthDate(),
                entity.getGender(),
                entity.getGuardianName(),
                entity.getGuardianPhone(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    StudentEntity toEntity(Student domain);

    List<Student> toDomain(List<StudentEntity> entities);
}
