package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.models.organization.OrganizationInvitation;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.OrganizationInvitationUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.InvitationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.InvitationDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.*;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Organization invitations")
@RequestMapping(path = "/api/organizations/{orgId}/invitations", version = "1.0")
public class OrganizationInvitationController {
    private final OrganizationInvitationUseCase useCase;
    private final InvitationDtoMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN') "
            + "and (#request.role().name() != 'SCHOOL_OWNER' or @authz.hasOrgRole('SCHOOL_OWNER'))")
    public InvitationDTO invite(@PathVariable Long orgId, @Valid @RequestBody InviteMemberRequest request) {
        return mapper.toDTO(useCase.invite(orgId, request.email(), request.role()));
    }

    @GetMapping
    @PreAuthorize("@authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN')")
    public PaginatedResult<InvitationDTO> list(
            @PathVariable Long orgId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<OrganizationInvitation> result =
                useCase.listPending(orgId, pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, mapper::toDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN')")
    public void revoke(@PathVariable Long orgId, @PathVariable Long id) {
        useCase.revoke(orgId, id);
    }

    @PostMapping("/accept")
    public OrganizationInvitation.AcceptanceResult accept(@Valid @RequestBody AcceptInvitationRequest request) {
        return useCase.accept(request.code(), request.password());
    }
}
