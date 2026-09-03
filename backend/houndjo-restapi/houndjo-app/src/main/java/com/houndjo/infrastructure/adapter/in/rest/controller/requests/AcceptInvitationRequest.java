package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(@NotBlank String code, String password) {}
