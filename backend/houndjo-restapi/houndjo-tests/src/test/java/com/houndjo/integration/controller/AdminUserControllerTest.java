package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.constants.DomainConstants;
import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.domain.enumerations.UserGroupConstants;
import com.houndjo.domain.enumerations.UserStatus;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.domain.models.userpreference.FontPreference;
import com.houndjo.domain.models.userpreference.ThemePreference;
import com.houndjo.domain.ports.out.NotificationSenderPort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PermissionCheckResponse;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PermissionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.UserDetailsDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.AssignRoleGroupRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateAdminUserRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateUserRequest;
import com.houndjo.infrastructure.adapter.out.persistence.entity.*;
import com.houndjo.infrastructure.adapter.out.persistence.repository.AppConfigurationRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.PermissionRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.RoleGroupRepository;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DirtiesContext
class AdminUserControllerTest extends IntegrationTest {

    private static final String API_ADMIN_USERS = "/api/admin/users";

    @MockitoBean
    private NotificationSenderPort notificationSenderPort;

    @Autowired
    private RoleGroupRepository roleGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AppConfigurationRepository appConfigurationRepository;

    @AfterEach
    void cleanupTwoFactorConfigs() {
        for (TwoFactorMethodType method : TwoFactorMethodType.values()) {
            appConfigurationRepository
                    .findByCategoryAndCode(AppConfigurationCategory.TWO_FACTOR, method.name())
                    .ifPresent(appConfigurationRepository::delete);
        }
    }

    // regions AdminUserController.createUser
    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldCreateUserAsAdminSuccessfully() throws Exception {
        CreateAdminUserRequest request = createValidCreateRequest("new-admin@example.com");

        UserDetailsDTO result = post(API_ADMIN_USERS, request, UserDetailsDTO.class, status().isCreated());

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(request.email().toLowerCase());
        assertThat(result.getPermissions()).contains("role-group:create", "role-group:update", "role-group:delete");

        Optional<UserEntity> savedUser = userRepository.findOneByUserCredentialsEmailIgnoreCase(request.email());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getRoleGroups())
                .extracting(RoleGroupEntity::getName)
                .containsExactly(UserGroupConstants.SYS_ADMIN);
    }

    // endregion

    // region AdminUserController.getUser
    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldGetUserAsAdminByIdSuccessfully() throws Exception {
        UserEntity user = createUser("admin-get@example.com", Set.of(UserGroupConstants.SYS_ADMIN));

        UserDetailsDTO result = get(API_ADMIN_USERS + "/" + user.getId(), new TypeReference<>() {}, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo(user.getUserCredentials().getEmail());
        assertThat(result.getPermissions()).contains("role-group:create", "role-group:update", "role-group:delete");
        assertThat(result.getPreferences().appearance().theme()).isEqualTo(ThemePreference.SYSTEM);
        assertThat(result.getPreferences().appearance().font()).isEqualTo(FontPreference.INTER);
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFailToGetUserAsAdminWhenNotFound() throws Exception {
        get(API_ADMIN_USERS + "/99999", status().isBadRequest());
    }

    // endregion

    // region AdminUserController.updateUser
    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldUpdateUserAsAdminSuccessfully() throws Exception {
        UserEntity user = createUser("admin-update@example.com", Set.of(UserGroupConstants.SYS_ADMIN));
        UpdateUserRequest request = new UpdateUserRequest(
                "Updated", "Admin", "123456789", LocalDate.of(1995, 4, 20), UserGender.FEMALE, "Conakry", "fr", null);

        UserDetailsDTO result =
                put(API_ADMIN_USERS + "/" + user.getId(), request, UserDetailsDTO.class, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Admin");
        assertThat(result.getBirthDate()).isEqualTo(request.birthDate());
        assertThat(result.getGender()).isEqualTo(request.gender());

        UserEntity updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getUserInfo().getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getUserInfo().getLastName()).isEqualTo("Admin");
        assertThat(updatedUser.getUserInfo().getBirthDate()).isEqualTo(request.birthDate());
        assertThat(updatedUser.getUserInfo().getGender()).isEqualTo(request.gender());
    }
    // endregion

    // region AdminUserController.deleteUser

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldDeleteUserAsAdminSuccessfully() throws Exception {
        UserEntity user = createUser("admin-delete@example.com", Set.of(UserGroupConstants.SYS_ADMIN));

        delete(API_ADMIN_USERS + "/" + user.getId(), status().isNoContent());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    // endregion

    // region AdminUserController.getUsers

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldGetAllUsersSuccessfully() throws Exception {
        createAdminUser("admin-list-1@example.com", "Mamadou", "Diallo", UserGender.MALE, UserStatus.ACTIVATED);
        createAdminUser("admin-list-2@example.com", "Mariama", "Bah", UserGender.FEMALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result = get(API_ADMIN_USERS, new TypeReference<>() {}, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(UserDetailsDTO::getEmail)
                .containsExactlyInAnyOrder("admin-list-1@example.com", "admin-list-2@example.com");
        assertThat(result.getItems())
                .extracting(user -> user.getPreferences().appearance().theme())
                .containsOnly(ThemePreference.SYSTEM);
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByEmailContains() throws Exception {
        createAdminUser("filter-alpha@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("filter-beta@example.com", "Bob", "Jones", UserGender.MALE, UserStatus.ACTIVATED);
        createAdminUser("other@example.com", "Charlie", "Brown", UserGender.MALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result =
                get(API_ADMIN_USERS + "?email.contains=filter-", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(UserDetailsDTO::getEmail)
                .containsExactlyInAnyOrder("filter-alpha@example.com", "filter-beta@example.com");
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByEmailEquals() throws Exception {
        createAdminUser("exact@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("other@example.com", "Bob", "Jones", UserGender.MALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result =
                get(API_ADMIN_USERS + "?email.equals=exact@example.com", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems().getFirst().getEmail()).isEqualTo("exact@example.com");
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByFirstNameContains() throws Exception {
        createAdminUser("user1@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("user2@example.com", "Alicia", "Jones", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("user3@example.com", "Bob", "Brown", UserGender.MALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result =
                get(API_ADMIN_USERS + "?firstName.contains=Ali", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(UserDetailsDTO::getFirstName)
                .containsExactlyInAnyOrder("Alice", "Alicia");
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByLastNameContains() throws Exception {
        createAdminUser("user1@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("user2@example.com", "Bob", "Smithson", UserGender.MALE, UserStatus.ACTIVATED);
        createAdminUser("user3@example.com", "Charlie", "Brown", UserGender.MALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result =
                get(API_ADMIN_USERS + "?lastName.contains=Smith", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(UserDetailsDTO::getLastName)
                .containsExactlyInAnyOrder("Smith", "Smithson");
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByGenderEquals() throws Exception {
        createAdminUser("male@example.com", "Bob", "Smith", UserGender.MALE, UserStatus.ACTIVATED);
        createAdminUser("female-1@example.com", "Alice", "Jones", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("female-2@example.com", "Carol", "Brown", UserGender.FEMALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result =
                get(API_ADMIN_USERS + "?gender.equals=FEMALE", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems()).extracting(UserDetailsDTO::getGender).containsOnly(UserGender.FEMALE);
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByStatusEquals() throws Exception {
        createAdminUser("active@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("inactive-1@example.com", "Bob", "Jones", UserGender.MALE, UserStatus.NOT_ACTIVATED);
        createAdminUser("inactive-2@example.com", "Charlie", "Brown", UserGender.MALE, UserStatus.NOT_ACTIVATED);

        PaginatedResult<UserDetailsDTO> result =
                get(API_ADMIN_USERS + "?status.equals=NOT_ACTIVATED", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems()).extracting(UserDetailsDTO::getStatus).containsOnly(UserStatus.NOT_ACTIVATED);
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByFirstNameIn() throws Exception {
        createAdminUser("user-alice@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("user-bob@example.com", "Bob", "Jones", UserGender.MALE, UserStatus.ACTIVATED);
        createAdminUser("user-charlie@example.com", "Charlie", "Brown", UserGender.MALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result = get(
                API_ADMIN_USERS + "?firstName.in=Alice&firstName.in=Bob", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(UserDetailsDTO::getFirstName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByEmailDoesNotContain() throws Exception {
        createAdminUser("keep-1@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("keep-2@example.com", "Bob", "Jones", UserGender.MALE, UserStatus.ACTIVATED);
        createAdminUser("exclude@example.com", "Charlie", "Brown", UserGender.MALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result =
                get(API_ADMIN_USERS + "?email.doesNotContain=exclude", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(UserDetailsDTO::getEmail)
                .containsExactlyInAnyOrder("keep-1@example.com", "keep-2@example.com");
    }

    // -------------------------------------------------------------------------
    // Filter tests — combined filters (AND semantics)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByCombinedFirstNameAndGender() throws Exception {
        createAdminUser("alice-f@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("alice-m@example.com", "Alice", "Jones", UserGender.MALE, UserStatus.ACTIVATED);
        createAdminUser("bob@example.com", "Bob", "Brown", UserGender.MALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result = get(
                API_ADMIN_USERS + "?firstName.contains=Alice&gender.equals=FEMALE",
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems().getFirst().getEmail()).isEqualTo("alice-f@example.com");
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByCombinedEmailAndStatus() throws Exception {
        createAdminUser("active-a@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("inactive-a@example.com", "Alice", "Jones", UserGender.FEMALE, UserStatus.NOT_ACTIVATED);
        createAdminUser("inactive-b@example.com", "Bob", "Brown", UserGender.MALE, UserStatus.NOT_ACTIVATED);

        PaginatedResult<UserDetailsDTO> result = get(
                API_ADMIN_USERS + "?email.contains=inactive&status.equals=NOT_ACTIVATED",
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(UserDetailsDTO::getEmail)
                .containsExactlyInAnyOrder("inactive-a@example.com", "inactive-b@example.com");
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldFilterByCombinedLastNameAndGenderAndStatus() throws Exception {
        createAdminUser("target@example.com", "Alice", "Martin", UserGender.FEMALE, UserStatus.ACTIVATED);
        createAdminUser("wrong-status@example.com", "Bob", "Martin", UserGender.MALE, UserStatus.ACTIVATED);
        createAdminUser("wrong-gender@example.com", "Carol", "Martin", UserGender.FEMALE, UserStatus.NOT_ACTIVATED);
        createAdminUser("wrong-name@example.com", "Dave", "Dupont", UserGender.FEMALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result = get(
                API_ADMIN_USERS + "?lastName.contains=Martin&gender.equals=FEMALE&status.equals=ACTIVATED",
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems().getFirst().getEmail()).isEqualTo("target@example.com");
    }

    // -------------------------------------------------------------------------
    // Filter tests — edge cases
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldReturnEmptyWhenNoFilterMatch() throws Exception {
        createAdminUser("user@example.com", "Alice", "Smith", UserGender.FEMALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> result =
                get(API_ADMIN_USERS + "?email.contains=nonexistent", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldReturnEmptyWhenNoUsersExist() throws Exception {
        PaginatedResult<UserDetailsDTO> result = get(API_ADMIN_USERS, new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getItems()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Pagination tests
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldSupportPagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createAdminUser(
                    "page-user-" + i + "@example.com", "User" + i, "Test", UserGender.MALE, UserStatus.ACTIVATED);
        }

        PaginatedResult<UserDetailsDTO> firstPage =
                get(API_ADMIN_USERS + "?page=0&size=2", new TypeReference<>() {}, status().isOk());

        assertThat(firstPage.getTotalItems()).isEqualTo(5);
        assertThat(firstPage.getItems()).hasSize(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.getPage()).isEqualTo(0);

        PaginatedResult<UserDetailsDTO> secondPage =
                get(API_ADMIN_USERS + "?page=1&size=2", new TypeReference<>() {}, status().isOk());

        assertThat(secondPage.getItems()).hasSize(2);
        assertThat(secondPage.getPage()).isEqualTo(1);
        assertThat(secondPage.getItems())
                .extracting(UserDetailsDTO::getEmail)
                .doesNotContainAnyElementsOf(firstPage.getItems().stream()
                        .map(UserDetailsDTO::getEmail)
                        .toList());

        PaginatedResult<UserDetailsDTO> lastPage =
                get(API_ADMIN_USERS + "?page=2&size=2", new TypeReference<>() {}, status().isOk());

        assertThat(lastPage.getItems()).hasSize(1);
        assertThat(lastPage.getPage()).isEqualTo(2);
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldSupportPaginationWithFilter() throws Exception {
        for (int i = 1; i <= 4; i++) {
            createAdminUser(
                    "paginate-" + i + "@example.com", "PaginateUser", "Test", UserGender.MALE, UserStatus.ACTIVATED);
        }
        createAdminUser("other@example.com", "Other", "Test", UserGender.MALE, UserStatus.ACTIVATED);

        PaginatedResult<UserDetailsDTO> firstPage = get(
                API_ADMIN_USERS + "?firstName.contains=PaginateUser&page=0&size=2",
                new TypeReference<>() {},
                status().isOk());

        assertThat(firstPage.getTotalItems()).isEqualTo(4);
        assertThat(firstPage.getItems()).hasSize(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);

        PaginatedResult<UserDetailsDTO> secondPage = get(
                API_ADMIN_USERS + "?firstName.contains=PaginateUser&page=1&size=2",
                new TypeReference<>() {},
                status().isOk());

        assertThat(secondPage.getItems()).hasSize(2);
        assertThat(secondPage.getItems())
                .extracting(UserDetailsDTO::getFirstName)
                .containsOnly("PaginateUser");
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidAccessForSimpleUser() throws Exception {
        get(API_ADMIN_USERS, status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldForbidAccessForAdmin() throws Exception {
        get(API_ADMIN_USERS, status().isForbidden());
    }

    // endregion

    // region AdminUserController.assignRoleGroup / revokeRoleGroup

    @Test
    @WithMockUser(authorities = {"user:read", "user:update"})
    void shouldAssignRoleGroupAsAdminToUserSuccessfully() throws Exception {
        UserEntity user = createUserWithoutRole("assign-role@example.com");
        RoleGroupEntity roleGroup = createRoleGroupForTest("Editors", "Can edit content");

        post(
                API_ADMIN_USERS + "/" + user.getId() + "/role-groups",
                new AssignRoleGroupRequest(roleGroup.getId()),
                status().isNoContent());

        UserEntity updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getRoleGroups()).extracting(RoleGroupEntity::getId).containsExactly(roleGroup.getId());
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:update"})
    void shouldFailToAssignRoleGroupWhenRoleGroupAsAdminNotFound() throws Exception {
        UserEntity user = createUser("assign-missing-role@example.com");

        post(
                API_ADMIN_USERS + "/" + user.getId() + "/role-groups",
                new AssignRoleGroupRequest(99999L),
                status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:update"})
    void shouldRevokeRoleGroupAsAdminFromUserSuccessfully() throws Exception {
        UserEntity user = createUserWithoutRole("revoke-role@example.com");
        RoleGroupEntity roleGroup = createRoleGroupForTest("Viewers", "Read-only access");

        post(
                API_ADMIN_USERS + "/" + user.getId() + "/role-groups",
                new AssignRoleGroupRequest(roleGroup.getId()),
                status().isNoContent());

        delete(API_ADMIN_USERS + "/" + user.getId() + "/role-groups/" + roleGroup.getId(), status().isNoContent());

        UserEntity updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getRoleGroups()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidRoleGroupAssignmentForSimpleUser() throws Exception {
        UserEntity user = createUser("forbidden-assign@example.com");
        post(
                API_ADMIN_USERS + "/" + user.getId() + "/role-groups",
                new AssignRoleGroupRequest(1L),
                status().isForbidden());
    }

    // endregion

    // region AdminUserController.getUserPermissions

    @Test
    @WithMockUser(authorities = {"user:read"})
    void shouldGetUserAsAdminPermissionsSuccessfully() throws Exception {
        UserEntity user = createUser("perm-user@example.com");
        RoleGroupEntity roleGroup = createRoleGroupForTest("PermGroup", "Permission test group");
        user.getRoleGroups().add(roleGroup);
        userRepository.save(user);

        List<PermissionDTO> result =
                get(API_ADMIN_USERS + "/" + user.getId() + "/permissions", new TypeReference<>() {}, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result).extracting(PermissionDTO::code).contains("user:read");
        assertThat(result).extracting(PermissionDTO::code).isSorted();
    }

    @Test
    @WithMockUser(authorities = {"user:read"})
    void shouldReturnEmptyPermissionsWhenUserHasNoRoleGroups() throws Exception {
        UserEntity user = createUserWithoutRole("no-role-group@example.com");

        List<PermissionDTO> result =
                get(API_ADMIN_USERS + "/" + user.getId() + "/permissions", new TypeReference<>() {}, status().isOk());

        assertThat(result).isEmpty();
    }

    @Test
    @WithMockUser(authorities = {"user:read"})
    void shouldFailToGetPermissionsWhenUserNotFound() throws Exception {
        get(API_ADMIN_USERS + "/99999/permissions", status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidGetPermissionsForSimpleUser() throws Exception {
        get(API_ADMIN_USERS + "/1/permissions", status().isForbidden());
    }

    // endregion

    // region AdminUserController.checkUserPermission

    @Test
    @WithMockUser(authorities = {"user:read"})
    void shouldReturnTrueWhenUserHasPermission() throws Exception {
        UserEntity user = createUser("check-perm@example.com");
        RoleGroupEntity roleGroup = createRoleGroupForTest("CheckGroup", "Check permission group");
        user.getRoleGroups().add(roleGroup);
        userRepository.save(user);

        PermissionCheckResponse result = get(
                API_ADMIN_USERS + "/" + user.getId() + "/permissions/check?code=user:read",
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.hasPermission()).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"user:read"})
    void shouldReturnFalseWhenUserHasNoRoleGroups() throws Exception {
        UserEntity user = createUser("check-no-perm@example.com");

        PermissionCheckResponse result = get(
                API_ADMIN_USERS + "/" + user.getId() + "/permissions/check?code=user:read",
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.hasPermission()).isFalse();
    }

    @Test
    @WithMockUser(authorities = {"user:read"})
    void shouldReturnFalseWhenPermissionNotInAssignedRoleGroups() throws Exception {
        UserEntity user = createUser("check-other-perm@example.com");
        // createRoleGroupForTest only includes "user:read" — check for a permission not in it
        RoleGroupEntity roleGroup = createRoleGroupForTest("LimitedGroup", "Limited group");
        user.getRoleGroups().add(roleGroup);
        userRepository.save(user);

        PermissionCheckResponse result = get(
                API_ADMIN_USERS + "/" + user.getId() + "/permissions/check?code=user:create",
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.hasPermission()).isFalse();
    }

    @Test
    @WithMockUser(authorities = {"user:read"})
    void shouldFailToCheckPermissionWhenUserNotFound() throws Exception {
        get(API_ADMIN_USERS + "/99999/permissions/check?code=user:read", status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidCheckPermissionForSimpleUser() throws Exception {
        get(API_ADMIN_USERS + "/1/permissions/check?code=user:read", status().isForbidden());
    }

    // endregion

    // region AdminUserController.createUser - default 2FA assignment

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldEnableTwoFactorByDefaultForManagedUserWithNonUserRoleWhenConfigured() throws Exception {
        createActiveTwoFactorConfig(TwoFactorMethodType.EMAIL);
        CreateAdminUserRequest request = createValidCreateRequest("2fa-sysadmin@example.com");

        post(API_ADMIN_USERS, request, UserDetailsDTO.class, status().isCreated());

        UserEntity savedUser = userRepository
                .findOneByUserCredentialsEmailIgnoreCase(request.email())
                .orElseThrow();
        assertThat(savedUser.isTwoFactorEnabled()).isTrue();
        assertThat(savedUser.getTwoFactorMethod()).isEqualTo(TwoFactorMethodType.EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldNotEnableTwoFactorByDefaultForManagedUserWithUserOnlyRoleWhenConfigured() throws Exception {
        createActiveTwoFactorConfig(TwoFactorMethodType.EMAIL);
        CreateAdminUserRequest request = new CreateAdminUserRequest(
                "2fa-user-only@example.com",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                UserGender.MALE,
                null,
                null,
                null,
                null,
                Set.of(UserGroupConstants.USER));

        post(API_ADMIN_USERS, request, UserDetailsDTO.class, status().isCreated());

        UserEntity savedUser = userRepository
                .findOneByUserCredentialsEmailIgnoreCase(request.email())
                .orElseThrow();
        assertThat(savedUser.isTwoFactorEnabled()).isFalse();
        assertThat(savedUser.getTwoFactorMethod()).isNull();
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldEnableTwoFactorByDefaultForManagedUserWithMixedRolesWhenConfigured() throws Exception {
        createActiveTwoFactorConfig(TwoFactorMethodType.EMAIL);
        CreateAdminUserRequest request = new CreateAdminUserRequest(
                "2fa-mixed@example.com",
                "Jane",
                "Doe",
                LocalDate.of(1990, 1, 1),
                UserGender.FEMALE,
                null,
                null,
                null,
                null,
                Set.of(UserGroupConstants.USER, UserGroupConstants.ADMIN));

        post(API_ADMIN_USERS, request, UserDetailsDTO.class, status().isCreated());

        UserEntity savedUser = userRepository
                .findOneByUserCredentialsEmailIgnoreCase(request.email())
                .orElseThrow();
        assertThat(savedUser.isTwoFactorEnabled()).isTrue();
        assertThat(savedUser.getTwoFactorMethod()).isEqualTo(TwoFactorMethodType.EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"user:read", "user:create", "user:update", "user:deactivate"})
    void shouldNotEnableTwoFactorByDefaultForManagedUserWhenNoActiveConfig() throws Exception {
        CreateAdminUserRequest request = createValidCreateRequest("2fa-no-config@example.com");

        post(API_ADMIN_USERS, request, UserDetailsDTO.class, status().isCreated());

        UserEntity savedUser = userRepository
                .findOneByUserCredentialsEmailIgnoreCase(request.email())
                .orElseThrow();
        assertThat(savedUser.isTwoFactorEnabled()).isFalse();
        assertThat(savedUser.getTwoFactorMethod()).isNull();
    }

    // endregion

    private static CreateAdminUserRequest createValidCreateRequest(String email) {
        return new CreateAdminUserRequest(
                email,
                "Mamadou",
                "Diallo",
                LocalDate.of(1990, 1, 1),
                UserGender.MALE,
                null,
                null,
                null,
                null,
                Set.of(UserGroupConstants.SYS_ADMIN) // roleGroupNames
                );
    }

    /**
     * Creates a user with the Admin role group and customizable profile data,
     * so they appear in the admin user list (which filters by Admin).
     */
    private void createAdminUser(
            String email, String firstName, String lastName, UserGender gender, UserStatus status) {
        RoleGroupEntity adminRoleGroup = roleGroupRepository
                .findByNameIn(Set.of(UserGroupConstants.ADMIN))
                .iterator()
                .next();

        UserEntity user = new UserEntity(null, null, null, null, new HashSet<>(), false, null, null);
        user.setUserInfo(new EmbeddableUserInfo(
                firstName,
                lastName,
                null,
                LocalDate.of(1990, 1, 1),
                gender,
                null,
                DomainConstants.DEFAULT_LANGUAGE,
                null));
        user.setUserCredentials(new EmbeddableCredentials(
                email.toLowerCase(),
                passwordEncoder.encode(DEFAULT_USER_PASSWORD),
                null,
                Instant.now(),
                null,
                null,
                null,
                null,
                null));
        user.setStatus(status);
        user.setRoleGroups(Set.of(adminRoleGroup));
        user.setCreationDate(Instant.now());
        user.setLastUpdateDate(Instant.now());
        user.setLastUpdatedBy("test");

        userRepository.save(user);
    }

    private RoleGroupEntity createRoleGroupForTest(String name, String description) {
        PermissionEntity permission = permissionRepository.findById("user:read").orElseThrow();
        RoleGroupEntity entity = new RoleGroupEntity(null, name, description, new HashSet<>(Set.of(permission)));
        entity.setCreationDate(Instant.now());
        entity.setLastUpdateDate(Instant.now());
        entity.setLastUpdatedBy("test");
        return roleGroupRepository.save(entity);
    }

    private AppConfigurationEntity createActiveTwoFactorConfig(TwoFactorMethodType method) {
        AppConfigurationEntity entity = new AppConfigurationEntity(
                null,
                AppConfigurationCategory.TWO_FACTOR,
                method.name(),
                method.name() + " two-factor authentication",
                null,
                true);
        entity.setCreationDate(Instant.now());
        entity.setLastUpdateDate(Instant.now());
        entity.setLastUpdatedBy("test");
        return appConfigurationRepository.save(entity);
    }
}
