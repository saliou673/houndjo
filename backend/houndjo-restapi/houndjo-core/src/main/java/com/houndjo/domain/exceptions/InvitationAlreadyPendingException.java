package com.houndjo.domain.exceptions;

/**
 * Thrown when inviting an email that already has a pending invitation in the same organization.
 */
public class InvitationAlreadyPendingException extends FunctionalException {
    public InvitationAlreadyPendingException(String email) {
        super("error.invitation.already-pending", "An invitation is already pending for " + email + ".", email);
    }
}
