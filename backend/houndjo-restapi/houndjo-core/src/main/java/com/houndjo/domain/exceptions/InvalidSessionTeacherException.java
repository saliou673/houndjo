package com.houndjo.domain.exceptions;

/**
 * Thrown when a user assigned to a session is not an active teacher in the session's organization.
 */
public class InvalidSessionTeacherException extends FunctionalException {
    public InvalidSessionTeacherException(Long userId) {
        super(
                "error.session.invalid-teacher",
                "User " + userId + " is not an active teacher in this organization.",
                userId);
    }
}
