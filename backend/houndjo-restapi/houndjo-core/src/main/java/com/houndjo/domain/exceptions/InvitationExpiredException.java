package com.houndjo.domain.exceptions;

public class InvitationExpiredException extends FunctionalException {
    public InvitationExpiredException() {
        super("error.invitation.expired", "The invitation has expired.");
    }
}
