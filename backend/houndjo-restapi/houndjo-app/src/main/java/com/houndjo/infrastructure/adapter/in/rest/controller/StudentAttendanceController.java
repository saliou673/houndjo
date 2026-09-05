package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.models.attendance.StudentAttendanceHistory;
import com.houndjo.domain.ports.in.AttendanceUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendanceHistoryDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.AttendanceDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for reviewing a student's attendance history within the active organization.
 */
@Validated
@RestController
@Tag(name = "Attendance management")
@PreAuthorize("hasAuthority('attendance:read')")
@RequestMapping(path = "/api/v1/students/{studentId}/attendance", version = "1.0")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final AttendanceUseCase attendanceUseCase;
    private final AttendanceDtoMapper attendanceDtoMapper;

    @GetMapping
    public AttendanceHistoryDTO getAttendanceHistory(
            @PathVariable Long studentId,
            @RequestParam(required = false) @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        StudentAttendanceHistory history = attendanceUseCase.getStudentHistory(studentId, from, to);
        return new AttendanceHistoryDTO(
                history.entries().stream().map(attendanceDtoMapper::toDTO).toList(), history.absenceRate());
    }
}
