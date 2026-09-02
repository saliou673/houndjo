package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import static com.houndjo.domain.constants.DomainConstants.EMAIL_REGEX_PATTERN;

import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.domain.enumerations.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a user, with his resolved permissions.
 * <p>:warning: Should only be used by admin endpoints.
 */
@Schema(name = "UserDetails")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserDetailsDTO extends AuditableDTO {
    /**
     * Unique identifier of the user.
     */
    private Long id;

    /**
     * Validated email address.
     */
    @Pattern(regexp = EMAIL_REGEX_PATTERN)
    @NotBlank
    private String email;

    /**
     * Optional phone number.
     */
    @Nullable
    private String phoneNumber;

    /**
     * Given name.
     */
    @NotBlank
    private String firstName;

    /**
     * Family name.
     */
    @NotBlank
    private String lastName;

    /**
     * Date of birth.
     */
    @NotNull
    private LocalDate birthDate;

    /**
     * Biological gender.
     */
    @NotNull
    private UserGender gender;

    /**
     * Optional postal address.
     */
    @Nullable
    private String address;

    /**
     * Current account lifecycle status.
     */
    @Nullable
    private UserStatus status;

    /**
     * Preferred locale key (e.g. {@code "fr"}).
     */
    private String languageKey;

    /**
     * Profile picture URL.
     */
    @Nullable
    private String imageUrl;

    /**
     * Flat list of permission codes resolved from the user's role groups.
     */
    @NotNull
    private List<String> permissions;

    /**
     * Role groups assigned to this user.
     */
    @NotNull
    private List<RoleGroupDTO> roleGroups;

    /**
     * Preferences owned by the user.
     */
    @NotNull
    private UserPreferencesDTO preferences;

    public UserDetailsDTO(
            Long id,
            String email,
            String firstName,
            String lastName,
            @Nullable String phoneNumber,
            LocalDate birthDate,
            UserGender gender,
            @Nullable String address,
            @Nonnull UserStatus status,
            String languageKey,
            @Nullable String imageUrl,
            @NotNull List<String> permissions,
            @NotNull List<RoleGroupDTO> roleGroups,
            @NotNull UserPreferencesDTO preferences,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(creationDate, lastUpdateDate, lastUpdatedBy);
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.status = status;
        this.languageKey = languageKey;
        this.imageUrl = imageUrl;
        this.permissions = permissions;
        this.roleGroups = roleGroups;
        this.preferences = preferences;
    }
}
