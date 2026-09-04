package com.houndjo.domain.exceptions;

/**
 * Thrown when accepting an invitation requires creating a new user but no password was supplied.
 */
public class InvitationPasswordRequiredException extends FunctionalException {
    public InvitationPasswordRequiredException() {
        super("error.invitation.password-required", "A password is required to accept this invitation.");
    }
}
