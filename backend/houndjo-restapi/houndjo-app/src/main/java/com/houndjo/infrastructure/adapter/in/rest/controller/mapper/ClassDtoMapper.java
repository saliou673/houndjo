package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link SchoolClass} to {@link ClassDTO}.
 */
@Component
public class ClassDtoMapper {

    public ClassDTO toDTO(SchoolClass schoolClass) {
        return toDTO(schoolClass, 0);
    }

    public ClassDTO toDTO(SchoolClass schoolClass, int courseCount) {
        return new ClassDTO(
                schoolClass.getId(),
                schoolClass.getName(),
                schoolClass.getDescription(),
                schoolClass.getDisplayOrder(),
                courseCount,
                schoolClass.getCreationDate());
    }
}
