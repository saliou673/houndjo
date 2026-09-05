package com.houndjo.domain.exceptions;

/**
 * Thrown when a leave authorization cannot be found by the given identifier within the active
 * organization.
 */
public class AttendancePermissionNotFoundException extends FunctionalException {
    public AttendancePermissionNotFoundException(Long id) {
        super("error.attendance-permission.not-found", "No attendance permission found with id " + id + ".", id);
    }
}
