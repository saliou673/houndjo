package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.enrollment.Enrollment;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.ports.in.SchoolClassUseCase;
import com.houndjo.domain.ports.in.StudentUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.EnrollmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Enrollment} to {@link EnrollmentDTO}, enriching it with the student's and class's
 * display names.
 */
@Component
@RequiredArgsConstructor
public class EnrollmentDtoMapper {

    private final StudentUseCase studentUseCase;
    private final SchoolClassUseCase schoolClassUseCase;

    public EnrollmentDTO toDTO(Enrollment enrollment) {
        Student student = studentUseCase.getById(enrollment.getStudentId());
        String className = schoolClassUseCase.getById(enrollment.getClassId()).getName();
        return new EnrollmentDTO(
                enrollment.getId(),
                enrollment.getStudentId(),
                student.getFirstName() + " " + student.getLastName(),
                enrollment.getClassId(),
                className,
                enrollment.getCourseIds(),
                enrollment.getStatus(),
                enrollment.getStartDate(),
                enrollment.getEndDate());
    }
}
