package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.domain.models.session.Session;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.ports.out.persistenceport.SessionPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendanceDTO;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Attendance} to {@link AttendanceDTO}, enriching it with the student's name and the
 * session's date.
 */
@Component
@RequiredArgsConstructor
public class AttendanceDtoMapper {

    private final StudentPersistencePort studentPersistencePort;
    private final SessionPersistencePort sessionPersistencePort;

    public AttendanceDTO toDTO(Attendance attendance) {
        return new AttendanceDTO(
                attendance.getId(),
                attendance.getStudentId(),
                studentName(attendance.getStudentId(), attendance.getOrganizationId()),
                attendance.getSessionId(),
                sessionDate(attendance.getSessionId(), attendance.getOrganizationId()),
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

    private LocalDate sessionDate(Long sessionId, Long organizationId) {
        return sessionPersistencePort
                .findByIdAndOrganizationId(sessionId, organizationId)
                .map(Session::getSessionDate)
                .orElse(null);
    }
}
