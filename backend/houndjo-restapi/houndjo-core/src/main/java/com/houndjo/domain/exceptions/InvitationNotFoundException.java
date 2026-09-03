package com.houndjo.domain.exceptions;

public class InvitationNotFoundException extends FunctionalException {
    public InvitationNotFoundException(String code) {
        super("error.invitation.not-found", "No invitation found for code " + code + ".", code);
    }
}
