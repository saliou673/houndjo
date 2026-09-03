package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.models.organization.OrganizationProfileUpdate;
import com.houndjo.domain.ports.in.AccountUseCase;
import com.houndjo.domain.ports.in.OrganizationUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.OrganizationDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateOrganizationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for organization (school) registration and lookup.
 */
@Validated
@RestController
@Tag(name = "Organization management")
@RequestMapping(path = "/api/organizations", version = "1.0")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationUseCase organizationUseCase;
    private final AccountUseCase accountUseCase;
    private final OrganizationDtoMapper organizationDtoMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationDTO registerSchool(@Valid @RequestBody RegisterSchoolRequest request) {
        Organization organization = Organization.create(
                request.name(),
                request.contactEmail(),
                request.phoneNumber(),
                request.address(),
                request.defaultCurrencyCode(),
                request.defaultLanguageKey());
        Long creatorUserId = accountUseCase.getCurrentUserWithAuthorities().getId();
        return organizationDtoMapper.toDTO(organizationUseCase.registerSchool(organization, creatorUserId));
    }

    @GetMapping("/mine")
    public List<OrganizationDTO> getMyOrganizations() {
        Long currentUserId = accountUseCase.getCurrentUserWithAuthorities().getId();
        return organizationUseCase.getMyOrganizations(currentUserId).stream()
                .map(organizationDtoMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('organization:read')")
    public OrganizationDTO getOrganizationById(@PathVariable Long id) {
        return organizationDtoMapper.toDTO(organizationUseCase.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN')")
    public OrganizationDTO updateOrganization(
            @PathVariable Long id, @Valid @RequestBody UpdateOrganizationRequest request) {
        OrganizationProfileUpdate update = new OrganizationProfileUpdate(
                request.name(),
                request.contactEmail(),
                request.phoneNumber(),
                request.address(),
                request.defaultCurrencyCode(),
                request.defaultLanguageKey(),
                request.timezone());
        return organizationDtoMapper.toDTO(organizationUseCase.updateProfile(id, update));
    }
}
