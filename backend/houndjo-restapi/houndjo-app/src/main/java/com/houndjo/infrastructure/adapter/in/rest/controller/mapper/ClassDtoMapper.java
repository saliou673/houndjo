package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link SchoolClass} to {@link ClassDTO}.
 * <p>
 * {@code courseCount} is hardcoded to 0 until the {@code Course} aggregate (E3-2) exists.
 */
@Component
public class ClassDtoMapper {

    public ClassDTO toDTO(SchoolClass schoolClass) {
        return new ClassDTO(
                schoolClass.getId(),
                schoolClass.getName(),
                schoolClass.getDescription(),
                schoolClass.getDisplayOrder(),
                0,
                schoolClass.getCreationDate());
    }
}
