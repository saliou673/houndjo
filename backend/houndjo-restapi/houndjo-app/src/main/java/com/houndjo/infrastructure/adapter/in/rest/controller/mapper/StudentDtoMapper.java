package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.student.Student;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.StudentDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Student} to {@link StudentDTO}. {@code userId} is intentionally omitted.
 */
@Component
public class StudentDtoMapper {

    public StudentDTO toDTO(Student student) {
        return new StudentDTO(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getBirthDate(),
                student.getGender(),
                student.getGuardianName(),
                student.getGuardianPhone(),
                student.getCreationDate());
    }
}
