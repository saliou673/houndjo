package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendanceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Attendance} to {@link AttendanceDTO}, enriching it with the student's name.
 */
@Component
@RequiredArgsConstructor
public class AttendanceDtoMapper {

    private final StudentPersistencePort studentPersistencePort;

    public AttendanceDTO toDTO(Attendance attendance) {
        return new AttendanceDTO(
                attendance.getId(),
                attendance.getStudentId(),
                studentName(attendance.getStudentId(), attendance.getOrganizationId()),
                attendance.getSessionId(),
                attendance.getStatus(),
                attendance.getReason());
    }

    private String studentName(Long studentId, Long organizationId) {
        return studentPersistencePort
                .findByIdAndOrganizationId(studentId, organizationId)
                .map(this::fullName)
                .orElse(null);
    }

    private String fullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }
}
