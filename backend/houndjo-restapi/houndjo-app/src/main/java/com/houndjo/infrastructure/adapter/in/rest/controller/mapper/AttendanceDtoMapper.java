package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.domain.models.session.Session;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.ports.out.persistenceport.SessionPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendanceDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Enrich attendance lists with two batched lookups per organization. */
@Component
@RequiredArgsConstructor
public class AttendanceDtoMapper {
    private final StudentPersistencePort studentPersistencePort;
    private final SessionPersistencePort sessionPersistencePort;

    public List<AttendanceDTO> toDTOs(List<Attendance> attendances) {
        Map<Long, Student> students = new HashMap<>();
        Map<Long, Session> sessions = new HashMap<>();
        attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getOrganizationId))
                .forEach((organizationId, entries) -> {
                    studentPersistencePort
                            .findByIdsAndOrganizationId(
                                    entries.stream()
                                            .map(Attendance::getStudentId)
                                            .collect(Collectors.toSet()),
                                    organizationId)
                            .forEach(student -> students.put(student.getId(), student));
                    sessionPersistencePort
                            .findByIdsAndOrganizationId(
                                    entries.stream()
                                            .map(Attendance::getSessionId)
                                            .collect(Collectors.toSet()),
                                    organizationId)
                            .forEach(session -> sessions.put(session.getId(), session));
                });
        return attendances.stream()
                .map(attendance -> {
                    Student student = students.get(attendance.getStudentId());
                    Session session = sessions.get(attendance.getSessionId());
                    return new AttendanceDTO(
                            attendance.getId(),
                            attendance.getStudentId(),
                            student == null ? null : student.getFirstName() + " " + student.getLastName(),
                            attendance.getSessionId(),
                            session == null ? null : session.getSessionDate(),
                            attendance.getStatus(),
                            attendance.getReason());
                })
                .toList();
    }
}
