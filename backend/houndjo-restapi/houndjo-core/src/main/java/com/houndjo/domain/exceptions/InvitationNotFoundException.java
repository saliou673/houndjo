package com.houndjo.domain.exceptions;

public class InvitationNotFoundException extends FunctionalException {
    public InvitationNotFoundException(String code) {
        super("error.invitation.not-found", "No invitation found for code " + code + ".", code);
    }

    public InvitationNotFoundException(Long id) {
        super("error.invitation.not-found-by-id", "No invitation found with id " + id + ".", id);
    }
}
