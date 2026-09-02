package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PermissionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.RoleGroupDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateRoleGroupRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateRoleGroupRequest;
import com.houndjo.infrastructure.adapter.out.persistence.entity.PermissionEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.RoleGroupEntity;
import com.houndjo.infrastructure.adapter.out.persistence.repository.PermissionRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.RoleGroupRepository;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

class AdminRoleGroupControllerTest extends IntegrationTest {

    private static final String API = "/api/admin/role-groups";

    @Autowired
    private RoleGroupRepository roleGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    // region create

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:create"})
    void shouldCreateRoleGroupAsAdminRoleGroupSuccessfully() throws Exception {
        CreateRoleGroupRequest request = new CreateRoleGroupRequest("Editors", "Can edit content", Set.of("user:read"));

        RoleGroupDTO result = post(API, request, RoleGroupDTO.class, status().isCreated());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Editors");
        assertThat(result.getDescription()).isEqualTo("Can edit content");
        assertThat(result.getPermissions()).hasSize(1);
        assertThat(result.getPermissions()).extracting(PermissionDTO::code).containsExactly("user:read");
        assertThat(roleGroupRepository.existsByName("Editors")).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:create"})
    void shouldFailToCreateRoleGroupAsAdminWithDuplicateName() throws Exception {
        createRoleGroupAsAdminRoleGroup("Editors", "First group");

        CreateRoleGroupRequest request = new CreateRoleGroupRequest("Editors", "Second group", Set.of("user:read"));

        post(API, request, status().isConflict());
    }

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:create"})
    void shouldFailToCreateRoleGroupAsAdminWithBlankName() throws Exception {
        CreateRoleGroupRequest request = new CreateRoleGroupRequest("", "Description", Set.of("user:read"));
        post(API, request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:create"})
    void shouldFailToCreateRoleGroupAsAdminWithBlankDescription() throws Exception {
        CreateRoleGroupRequest request = new CreateRoleGroupRequest("Name", "", Set.of("user:read"));
        post(API, request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:create"})
    void shouldFailToCreateRoleGroupAsAdminWithEmptyPermissions() throws Exception {
        CreateRoleGroupRequest request = new CreateRoleGroupRequest("Name", "Description", Set.of());
        post(API, request, status().isBadRequest());
    }

    // endregion

    // region getById

    @Test
    @WithMockUser(authorities = "role-group:read")
    void shouldGetRoleGroupByIdSuccessfully() throws Exception {
        RoleGroupEntity entity = createRoleGroupAsAdminRoleGroup("Editors", "Can edit content");

        RoleGroupDTO result = get(API + "/" + entity.getId(), new TypeReference<>() {}, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getName()).isEqualTo("Editors");
        assertThat(result.getDescription()).isEqualTo("Can edit content");
    }

    @Test
    @WithMockUser(authorities = "role-group:read")
    void shouldFailToGetRoleGroupWhenNotFound() throws Exception {
        get(API + "/99999", status().isNotFound());
    }

    // endregion

    // region findAll

    @Test
    @WithMockUser(authorities = "role-group:read")
    void shouldGetRoleGroupsAsAdminRoleGroupsSuccessfully() throws Exception {
        createRoleGroupAsAdminRoleGroup("Group A", "First group");
        createRoleGroupAsAdminRoleGroup("Group B", "Second group");

        PaginatedResult<RoleGroupDTO> result = get(API, new TypeReference<>() {}, status().isOk());

        assertThat(result.getItems()).extracting(RoleGroupDTO::getName).contains("Group A", "Group B");
    }

    @Test
    @WithMockUser(authorities = "role-group:read")
    void shouldReturnSeededRoleGroupsByDefault() throws Exception {
        PaginatedResult<RoleGroupDTO> result = get(API, new TypeReference<>() {}, status().isOk());

        // 6 default role groups are seeded by DML migrations
        assertThat(result.getItems()).hasSize(4);
    }

    // endregion

    // region update

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:update"})
    void shouldUpdateRoleGroupAsAdminRoleGroupSuccessfully() throws Exception {
        RoleGroupEntity entity = createRoleGroupAsAdminRoleGroup("Old Name", "Old description");

        UpdateRoleGroupRequest request =
                new UpdateRoleGroupRequest("New Name", "New description", Set.of("role-group:update"));

        RoleGroupDTO result = put(API + "/" + entity.getId(), request, RoleGroupDTO.class, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getPermissions()).extracting(PermissionDTO::code).containsExactly("role-group:update");

        RoleGroupEntity updated = roleGroupRepository.findById(entity.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:update"})
    void shouldAllowUpdateRoleGroupAsAdminWithSameName() throws Exception {
        RoleGroupEntity entity = createRoleGroupAsAdminRoleGroup("Same Name", "Description");

        UpdateRoleGroupRequest request =
                new UpdateRoleGroupRequest("Same Name", "Updated description", Set.of("user:read"));

        RoleGroupDTO result = put(API + "/" + entity.getId(), request, RoleGroupDTO.class, status().isOk());

        assertThat(result.getName()).isEqualTo("Same Name");
        assertThat(result.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:update"})
    void shouldFailToUpdateRoleGroupAsAdminWithDuplicateName() throws Exception {
        createRoleGroupAsAdminRoleGroup("Existing Name", "Group 1");
        RoleGroupEntity second = createRoleGroupAsAdminRoleGroup("Second Group", "Group 2");

        UpdateRoleGroupRequest request =
                new UpdateRoleGroupRequest("Existing Name", "Updated description", Set.of("user:read"));

        put(API + "/" + second.getId(), request, status().isConflict());
    }

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:update"})
    void shouldFailToUpdateRoleGroupAsAdminWhenNotFound() throws Exception {
        UpdateRoleGroupRequest request = new UpdateRoleGroupRequest("Name", "Description", Set.of("user:read"));

        put(API + "/99999", request, status().isNotFound());
    }

    // endregion

    // region delete

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:delete"})
    void shouldDeleteRoleGroupAsAdminRoleGroupSuccessfully() throws Exception {
        RoleGroupEntity entity = createRoleGroupAsAdminRoleGroup("To Delete", "Will be deleted");

        delete(API + "/" + entity.getId(), status().isNoContent());

        assertThat(roleGroupRepository.findById(entity.getId())).isEmpty();
    }

    @Test
    @WithMockUser(authorities = {"role-group:read", "role-group:delete"})
    void shouldFailToDeleteRoleGroupAsAdminWhenNotFound() throws Exception {
        delete(API + "/99999", status().isNotFound());
    }

    // endregion

    // region listPermissions

    @Test
    @WithMockUser(authorities = "role-group:read")
    void shouldListAllPermissionsSuccessfully() throws Exception {
        PaginatedResult<PermissionDTO> result = get(API + "/permissions", new TypeReference<>() {}, status().isOk());

        assertThat(result.getItems()).isNotEmpty();
        assertThat(result.getItems())
                .extracting(PermissionDTO::code)
                .contains(
                        "role-group:read",
                        "role-group:create",
                        "role-group:update",
                        "role-group:delete",
                        "user:read",
                        "config:read",
                        "config:create",
                        "config:update",
                        "config:delete");
    }

    // endregion

    // region security

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidReadAccessForUserWithNoPermissions() throws Exception {
        get(API, status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "role-group:read")
    void shouldForbidCreateRoleGroupAsAdminForUserWithReadOnlyPermission() throws Exception {
        CreateRoleGroupRequest request = new CreateRoleGroupRequest("Name", "Description", Set.of("user:read"));
        post(API, request, status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "role-group:read")
    void shouldForbidDeleteRoleGroupAsAdminForUserWithReadOnlyPermission() throws Exception {
        RoleGroupEntity entity = createRoleGroupAsAdminRoleGroup("Group", "Description");

        delete(API + "/" + entity.getId(), status().isForbidden());
    }

    // endregion

    private RoleGroupEntity createRoleGroupAsAdminRoleGroup(String name, String description) {
        PermissionEntity permission = permissionRepository.findById("user:read").orElseThrow();
        RoleGroupEntity entity = new RoleGroupEntity(null, name, description, new HashSet<>(Set.of(permission)));
        entity.setCreationDate(Instant.now());
        entity.setLastUpdateDate(Instant.now());
        entity.setLastUpdatedBy("test");
        return roleGroupRepository.save(entity);
    }
}
