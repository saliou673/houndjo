package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserFilter;
import com.houndjo.domain.models.user.UserInfoUpdate;
import com.houndjo.domain.ports.in.AccountUseCase;
import com.houndjo.domain.ports.in.RoleGroupUseCase;
import com.houndjo.domain.ports.in.UserQueryUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PermissionCheckResponse;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PermissionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.UserDetailsDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.CreateAdminUserRequestMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.PermissionDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.UpdateUserRequestMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.UserDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.AssignRoleGroupRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateAdminUserRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateUserRequest;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AuditableEntity_;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Comparator;
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
 * REST controller for admin user management.
 */
@Validated
@RestController
@Tag(name = "Admin user management")
@RequestMapping(path = "/api/admin/users", version = "1.0")
@RequiredArgsConstructor
public class AdminUserController {

    private final AccountUseCase accountUseCase;
    private final UserQueryUseCase userQueryUseCase;
    private final RoleGroupUseCase roleGroupUseCase;
    private final CreateAdminUserRequestMapper createAdminUserRequestMapper;
    private final UpdateUserRequestMapper updateUserRequestMapper;
    private final UserDtoMapper userDtoMapper;
    private final PermissionDtoMapper permissionDtoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('user:create')")
    public UserDetailsDTO createUserAsAdmin(@Valid @RequestBody CreateAdminUserRequest request) {
        return userDtoMapper.toDetailsDTO(accountUseCase.createManagedUser(
                createAdminUserRequestMapper.toDomain(request),
                createAdminUserRequestMapper.toRoleGroupNames(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public UserDetailsDTO getUserAsAdmin(@PathVariable Long id) {
        return userDtoMapper.toDetailsDTO(accountUseCase.getUserWithAuthoritiesById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public PaginatedResult<UserDetailsDTO> getUsersAsAdmin(
            UserFilter filter,
            @PageableDefault(
                            size = DEFAULT_PAGE_SIZE_INT,
                            sort = AuditableEntity_.CREATION_DATE,
                            direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<User> result = userQueryUseCase.findAll(filter, pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, userDtoMapper::toDetailsDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public UserDetailsDTO updateUserAsAdmin(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserInfoUpdate infoUpdate = updateUserRequestMapper.toDomain(request);
        return userDtoMapper.toDetailsDTO(accountUseCase.updateUserById(id, infoUpdate));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user:deactivate')")
    public void deleteUserAsAdmin(@PathVariable Long id) {
        accountUseCase.deleteUserById(id);
    }

    @PostMapping("/{id}/role-groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user:update')")
    public void assignRoleGroupAsAdmin(@PathVariable Long id, @Valid @RequestBody AssignRoleGroupRequest request) {
        roleGroupUseCase.assignToUser(id, request.roleGroupId());
    }

    @DeleteMapping("/{id}/role-groups/{roleGroupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user:update')")
    public void revokeRoleGroupAsAdmin(@PathVariable Long id, @PathVariable Long roleGroupId) {
        roleGroupUseCase.revokeFromUser(id, roleGroupId);
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('user:read')")
    public List<PermissionDTO> getUserPermissionsAsAdmin(@PathVariable Long id) {
        return accountUseCase.getUserWithAuthoritiesById(id).resolvePermissions().stream()
                .sorted(Comparator.comparing(Permission::code))
                .map(permissionDtoMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}/permissions/check")
    @PreAuthorize("hasAuthority('user:read')")
    public PermissionCheckResponse checkUserPermissionAsAdmin(@PathVariable Long id, @RequestParam String code) {
        boolean hasPermission = accountUseCase.getUserWithAuthoritiesById(id).resolvePermissions().stream()
                .anyMatch(p -> p.code().equals(code));
        return new PermissionCheckResponse(hasPermission);
    }
}
