package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.domain.models.attendance.AttendanceEntry;
import com.houndjo.domain.ports.in.AttendanceUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendanceDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.AttendanceDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.BulkAttendanceRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for recording and reviewing a session's roll-call attendance within the active
 * organization.
 */
@Validated
@RestController
@Tag(name = "Attendance management")
@PreAuthorize("hasAuthority('attendance:read')")
@RequestMapping(path = "/api/v1/sessions/{sessionId}/attendance", version = "1.0")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceUseCase attendanceUseCase;
    private final AttendanceDtoMapper attendanceDtoMapper;

    @GetMapping
    public List<AttendanceDTO> getAttendance(@PathVariable Long sessionId) {
        return attendanceDtoMapper.toDTOs(attendanceUseCase.findBySession(sessionId));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('attendance:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public List<AttendanceDTO> recordBulkAttendance(
            @PathVariable Long sessionId, @Valid @RequestBody BulkAttendanceRequest request) {
        List<AttendanceEntry> entries = request.entries().stream()
                .map(entry -> new AttendanceEntry(entry.studentId(), entry.status(), entry.reason()))
                .toList();
        List<Attendance> recorded = attendanceUseCase.recordBulk(sessionId, entries);
        return attendanceDtoMapper.toDTOs(recorded);
    }
}
