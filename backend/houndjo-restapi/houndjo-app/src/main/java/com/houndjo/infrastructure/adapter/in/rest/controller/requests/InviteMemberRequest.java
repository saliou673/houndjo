package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.OrganizationRole;
import jakarta.validation.constraints.*;

public record InviteMemberRequest(
        @NotBlank @Email String email, @NotNull OrganizationRole role) {}
