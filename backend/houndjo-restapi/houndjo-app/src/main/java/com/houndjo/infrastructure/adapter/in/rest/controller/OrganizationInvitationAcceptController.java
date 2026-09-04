package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.models.organization.OrganizationInvitation;
import com.houndjo.domain.ports.in.OrganizationInvitationUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.AcceptInvitationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Accepts an organization invitation by code.
 * <p>
 * Kept separate from {@link OrganizationInvitationController}: the invitation code alone
 * identifies the target organization, so this endpoint carries no {@code orgId} path variable
 * and, unlike the rest of that controller, is reachable without authentication (see
 * {@code SecurityConfiguration.PUBLIC_ROUTES}) since the caller may not have an account yet.
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Organization invitations")
@RequestMapping(path = "/api/organizations/invitations/accept", version = "1.0")
public class OrganizationInvitationAcceptController {
    private final OrganizationInvitationUseCase useCase;

    @PostMapping
    public OrganizationInvitation.AcceptanceResult accept(@Valid @RequestBody AcceptInvitationRequest request) {
        return useCase.accept(request.code(), request.password());
    }
}
