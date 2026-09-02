package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserInfoUpdate;
import com.houndjo.domain.ports.in.AccountUseCase;
import com.houndjo.domain.ports.in.UserPreferenceUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PermissionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.UserPreferencesDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.UserSummaryDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.CreateUserRequestMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.PermissionDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.UpdateUserRequestMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.UserDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.UserPreferencesDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the current user's account.
 */
@Validated
@RestController
@Tag(name = "User account management")
@RequestMapping(path = "/api/accounts", version = "1.0")
@RequiredArgsConstructor
public class UserAccountController {

    private final AccountUseCase accountUseCase;
    private final UserPreferenceUseCase userPreferenceUseCase;
    private final CreateUserRequestMapper createUserRequestMapper;
    private final UserDtoMapper userDtoMapper;
    private final UpdateUserRequestMapper updateUserRequestMapper;
    private final PermissionDtoMapper permissionDtoMapper;
    private final UserPreferencesDtoMapper userPreferencesDtoMapper;

    /**
     * Creates a new public user account.
     *
     * @param request the request body containing the user information.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPublicUserAccount(@Valid @RequestBody CreateUserRequest request) {
        accountUseCase.createPublicUser(createUserRequestMapper.toDomain(request));
    }

    /**
     * Activate a registered user.
     *
     * @param code the activation code (OTP).
     */
    @GetMapping("/activation")
    public void activateAccount(@RequestParam String code) {
        accountUseCase.activateRegistration(code);
    }

    /**
     * Send new confirmation email to user.
     *
     * @param email the mail of the user.
     */
    @PostMapping(path = "/activation/resend", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void requestActivationCode(@RequestBody String email) {
        accountUseCase.sendActivationCode(email);
    }

    /**
     * Get the current user.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('user:read:own')")
    public UserSummaryDTO getUserDetails() {
        User user = accountUseCase.getCurrentUserWithAuthorities();
        return userDtoMapper.toSummaryDTO(user);
    }

    /**
     * Get resolved permissions of the current user (flattened from role groups).
     */
    @GetMapping("/me/permissions")
    @PreAuthorize("hasAuthority('user:read:own')")
    public List<PermissionDTO> getCurrentUserPermissions() {
        return accountUseCase.getCurrentUserWithAuthorities().resolvePermissions().stream()
                .sorted(Comparator.comparing(Permission::code))
                .map(permissionDtoMapper::toDTO)
                .toList();
    }

    /**
     * Update the current user information.
     */
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('user:update:own')")
    public UserSummaryDTO updateAccount(@Valid @RequestBody UpdateUserRequest request) {
        UserInfoUpdate infoUpdate = updateUserRequestMapper.toDomain(request);
        return userDtoMapper.toSummaryDTO(accountUseCase.updateCurrentUser(infoUpdate));
    }

    /**
     * Returns the current user's preferences.
     */
    @GetMapping("/me/preferences")
    @PreAuthorize("hasAuthority('user:read:own')")
    public UserPreferencesDTO getCurrentUserPreferences() {
        return userPreferencesDtoMapper.toDTO(userPreferenceUseCase.getCurrentUserPreferences());
    }

    /**
     * Updates the current user's preferences.
     */
    @PutMapping("/me/preferences")
    @PreAuthorize("hasAuthority('user:update:own')")
    public UserPreferencesDTO updateCurrentUserPreferences(@Valid @RequestBody UserPreferencesDTO preferences) {
        return userPreferencesDtoMapper.toDTO(
                userPreferenceUseCase.updateCurrentUserPreferences(userPreferencesDtoMapper.toDomain(preferences)));
    }

    /**
     * Soft-delete the current user account.
     */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user:update:own')")
    public void deleteCurrentAccount() {
        accountUseCase.deleteCurrentUserAccount();
    }

    /**
     * Recover a previously soft-deleted account.
     *
     * @param request account credentials.
     */
    @PostMapping(path = "/recover")
    @ResponseStatus(HttpStatus.OK)
    public void recoverAccount(@Valid @RequestBody RecoverAccountRequest request) {
        accountUseCase.recoverAccount(request.email(), request.password());
    }

    /**
     * Changes the current user's password.
     *
     * @param request current and new password.
     */
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('user:update:own')")
    public void changePassword(@RequestBody PasswordChangeRequest request) {
        accountUseCase.changePassword(request.currentPassword(), request.newPassword());
    }

    /**
     * Send email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/reset-password/init", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void requestPasswordReset(@RequestBody String mail) {
        accountUseCase.requestPasswordReset(mail);
    }

    /**
     * Finish resetting the password of the user.
     *
     * @param request the generated code and the new password.
     */
    @PostMapping(path = "/reset-password/finish")
    @ResponseStatus(HttpStatus.OK)
    public void finishPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        accountUseCase.completePasswordReset(request.newPassword(), request.code());
    }

    /**
     * Complete a managed user invitation by setting the initial password.
     *
     * @param request the invitation code and the chosen password.
     */
    @PostMapping(path = "/invitation/complete")
    @ResponseStatus(HttpStatus.OK)
    public void completeInvitation(@RequestBody @Valid InvitationCompleteRequest request) {
        accountUseCase.completeInvitation(request.code(), request.newPassword());
    }

    /**
     * Initiates an email-change flow by sending an OTP to the new email address.
     *
     * @param request the desired new email address.
     */
    @PostMapping("/me/email/change-request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user:update:own')")
    public void requestEmailChange(@RequestBody @Valid EmailChangeRequest request) {
        accountUseCase.requestEmailChange(request.newEmail());
    }

    /**
     * Confirms an email-change request by validating the OTP code.
     *
     * @param request the OTP code sent to the new email address.
     */
    @PostMapping("/me/email/change-confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user:update:own')")
    public void confirmEmailChange(@RequestBody @Valid EmailChangeConfirmRequest request) {
        accountUseCase.confirmEmailChange(request.code());
    }
}
