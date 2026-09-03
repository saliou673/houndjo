package com.houndjo.domain.exceptions;

public class InvitationRevokedException extends FunctionalException {
    public InvitationRevokedException() {
        super("error.invitation.revoked", "The invitation has been revoked.");
    }
}
