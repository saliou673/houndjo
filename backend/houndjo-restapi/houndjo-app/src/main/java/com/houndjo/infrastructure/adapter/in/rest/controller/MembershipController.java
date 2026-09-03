package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.MembershipUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.MembershipDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.MembershipDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.ChangeMembershipRoleRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing an organization's memberships.
 */
@Validated
@RestController
@Tag(name = "Membership management")
@PreAuthorize("hasAuthority('membership:read')")
@RequestMapping(path = "/api/organizations/{orgId}/memberships", version = "1.0")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipUseCase membershipUseCase;
    private final MembershipDtoMapper membershipDtoMapper;

    @GetMapping
    public PaginatedResult<MembershipDTO> getMemberships(
            @PathVariable Long orgId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<Membership> result =
                membershipUseCase.findByOrganizationId(orgId, pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, membershipDtoMapper::toDTO);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('membership:update')")
    public MembershipDTO changeMembershipRole(
            @PathVariable Long orgId, @PathVariable Long id, @Valid @RequestBody ChangeMembershipRoleRequest request) {
        return membershipDtoMapper.toDTO(membershipUseCase.changeRole(orgId, id, request.role()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('membership:delete')")
    public void revokeMembership(@PathVariable Long orgId, @PathVariable Long id) {
        membershipUseCase.revoke(orgId, id);
    }
}
