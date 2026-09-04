package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.domain.ports.out.persistenceport.CoursePersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps {@link SchoolClass} to {@link ClassDTO}.
 */
@Component
@RequiredArgsConstructor
public class ClassDtoMapper {

    private final CoursePersistencePort coursePersistencePort;

    public ClassDTO toDTO(SchoolClass schoolClass) {
        int courseCount = (int)
                coursePersistencePort.countByClassIdAndOrganizationId(schoolClass.getId(), schoolClass.getOrganizationId());
        return new ClassDTO(
                schoolClass.getId(),
                schoolClass.getName(),
                schoolClass.getDescription(),
                schoolClass.getDisplayOrder(),
                courseCount,
                schoolClass.getCreationDate());
    }
}
