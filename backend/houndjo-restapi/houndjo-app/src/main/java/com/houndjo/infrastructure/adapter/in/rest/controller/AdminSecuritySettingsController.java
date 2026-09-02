package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.ports.in.SecuritySettingsUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SecuritySettingsDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.SecuritySettingsDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpsertSecuritySettingsRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin security settings management.
 */
@Validated
@RestController
@Tag(name = "Admin security settings management")
@RequestMapping(path = "/api/admin/security-settings", version = "1.0")
@RequiredArgsConstructor
public class AdminSecuritySettingsController {

    private final SecuritySettingsUseCase securitySettingsUseCase;
    private final SecuritySettingsDtoMapper securitySettingsDtoMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('config:read')")
    public SecuritySettingsDTO getSecuritySettingsAsAdmin() {
        return securitySettingsDtoMapper.toDTO(securitySettingsUseCase.get());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('config:update')")
    public SecuritySettingsDTO upsertSecuritySettingsAsAdmin(
            @Valid @RequestBody UpsertSecuritySettingsRequest request) {
        return securitySettingsDtoMapper.toDTO(securitySettingsUseCase.upsert(request.twoFactorRequired()));
    }
}
