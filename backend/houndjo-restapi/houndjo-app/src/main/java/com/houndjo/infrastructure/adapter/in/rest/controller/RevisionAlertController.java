package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.ports.in.ProgressStateUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.RevisionAlertDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.ProgressStateDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the organization-wide revision-alerts aggregate, for the dashboard.
 */
@Validated
@RestController
@Tag(name = "Progress tracking")
@PreAuthorize("hasAuthority('progress:read')")
@RequestMapping(path = "/api/v1/organizations/{orgId}/revision-alerts", version = "1.0")
@RequiredArgsConstructor
public class RevisionAlertController {

    private final ProgressStateUseCase progressStateUseCase;
    private final ProgressStateDtoMapper progressStateDtoMapper;

    @GetMapping
    public List<RevisionAlertDTO> getRevisionAlerts(@PathVariable Long orgId) {
        return progressStateUseCase.getRevisionAlerts(orgId).stream()
                .map(progressStateDtoMapper::toDTO)
                .toList();
    }
}
