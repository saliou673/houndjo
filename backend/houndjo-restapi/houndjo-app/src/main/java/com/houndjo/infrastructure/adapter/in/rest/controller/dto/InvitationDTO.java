package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.InvitationStatus;
import com.houndjo.domain.enumerations.OrganizationRole;
import java.time.Instant;

public record InvitationDTO(
        Long id,
        String email,
        OrganizationRole role,
        InvitationStatus status,
        Instant expiresAt,
        Instant creationDate) {}
