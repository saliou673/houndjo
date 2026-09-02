package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.domain.models.appconfiguration.AppConfiguration;
import com.houndjo.domain.models.appconfiguration.AppConfigurationFilter;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.AppConfigurationQueryUseCase;
import com.houndjo.domain.ports.in.AppConfigurationUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AppConfigurationCategoryDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AppConfigurationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.AppConfigurationDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateAppConfigurationRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateAppConfigurationRequest;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AuditableEntity_;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin application configuration management.
 */
@Validated
@RestController
@Tag(name = "Admin configuration management")
@RequestMapping(path = "/api/admin/configurations", version = "1.0")
@RequiredArgsConstructor
public class AdminAppConfigurationController {

    private final AppConfigurationUseCase appConfigurationUseCase;
    private final AppConfigurationQueryUseCase appConfigurationQueryUseCase;
    private final AppConfigurationDtoMapper appConfigurationDtoMapper;

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('config:read')")
    public List<AppConfigurationCategoryDTO> getCategoriesAsAdmin() {
        return Arrays.stream(AppConfigurationCategory.values())
                .map(AppConfigurationCategoryDTO::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('config:create')")
    public AppConfigurationDTO createAppConfigurationAsAdmin(
            @Valid @RequestBody CreateAppConfigurationRequest request) {
        return appConfigurationDtoMapper.toDTO(appConfigurationUseCase.create(
                request.category(), request.code(), request.label(), request.description()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('config:read')")
    public AppConfigurationDTO getAppConfigurationByIdAsAdmin(@PathVariable Long id) {
        return appConfigurationDtoMapper.toDTO(appConfigurationUseCase.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('config:read')")
    public PaginatedResult<AppConfigurationDTO> getAppConfigurationsAsAdmin(
            AppConfigurationFilter filter,
            @PageableDefault(
                            size = DEFAULT_PAGE_SIZE_INT,
                            sort = AuditableEntity_.CREATION_DATE,
                            direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<AppConfiguration> result =
                appConfigurationQueryUseCase.findAll(filter, pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, appConfigurationDtoMapper::toDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('config:update')")
    public AppConfigurationDTO updateAppConfigurationAsAdmin(
            @PathVariable Long id, @Valid @RequestBody UpdateAppConfigurationRequest request) {
        return appConfigurationDtoMapper.toDTO(appConfigurationUseCase.update(
                id, request.code(), request.label(), request.description(), Boolean.TRUE.equals(request.active())));
    }

    @PutMapping("/{category}/{code}")
    @PreAuthorize("hasAuthority('config:update')")
    public AppConfigurationDTO updateByCategoryAndCode(
            @PathVariable AppConfigurationCategory category,
            @PathVariable String code,
            @Valid @RequestBody UpdateAppConfigurationRequest request) {
        return appConfigurationDtoMapper.toDTO(appConfigurationUseCase.updateByCategoryAndCode(
                category,
                code,
                request.code(),
                request.label(),
                request.description(),
                Boolean.TRUE.equals(request.active())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('config:delete')")
    public void delete(@PathVariable Long id) {
        appConfigurationUseCase.delete(id);
    }
}
