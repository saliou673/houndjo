package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.attendance.AttendancePermission;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendancePermissionDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link AttendancePermission} to {@link AttendancePermissionDTO}.
 */
@Component
public class AttendancePermissionDtoMapper {

    public AttendancePermissionDTO toDTO(AttendancePermission attendancePermission) {
        return new AttendancePermissionDTO(
                attendancePermission.getId(),
                attendancePermission.getStudentId(),
                attendancePermission.getFromDate(),
                attendancePermission.getToDate(),
                attendancePermission.getReason(),
                attendancePermission.getStatus());
    }
}
