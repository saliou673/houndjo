package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.ports.in.AttendancePermissionUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendancePermissionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.AttendancePermissionDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateAttendancePermissionRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateAttendancePermissionStatusRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing student leave/absence authorizations within the active
 * organization. Named {@code attendance-permission} to avoid colliding with the platform's RBAC
 * {@code Permission} resource.
 */
@Validated
@RestController
@Tag(name = "Attendance management")
@PreAuthorize("hasAuthority('attendance-permission:read')")
@RequestMapping(path = "/api/v1/attendance-permissions", version = "1.0")
@RequiredArgsConstructor
public class AttendancePermissionController {

    private final AttendancePermissionUseCase attendancePermissionUseCase;
    private final AttendancePermissionDtoMapper attendancePermissionDtoMapper;

    @GetMapping("/{id}")
    public AttendancePermissionDTO getAttendancePermissionById(@PathVariable Long id) {
        return attendancePermissionDtoMapper.toDTO(attendancePermissionUseCase.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAuthority('attendance-permission:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public AttendancePermissionDTO createAttendancePermission(
            @Valid @RequestBody CreateAttendancePermissionRequest request) {
        return attendancePermissionDtoMapper.toDTO(attendancePermissionUseCase.create(
                request.studentId(), request.fromDate(), request.toDate(), request.reason()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('attendance-permission:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public AttendancePermissionDTO updateAttendancePermissionStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateAttendancePermissionStatusRequest request) {
        return attendancePermissionDtoMapper.toDTO(attendancePermissionUseCase.updateStatus(id, request.status()));
    }
}
