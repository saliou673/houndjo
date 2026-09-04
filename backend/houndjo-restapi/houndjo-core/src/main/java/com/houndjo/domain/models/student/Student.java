package com.houndjo.domain.models.student;

import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate representing a student's pedagogical profile within an organization. A student is
 * creatable without a linked {@code User} account; {@code userId} is reserved for future account
 * linking and is never exposed through the API.
 */
@Getter
public class Student extends Auditable<Long> {

    private final Long organizationId;
    private final Long userId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private UserGender gender;
    private String guardianName;
    private String guardianPhone;

    private Student(
            Long id,
            Long organizationId,
            Long userId,
            String firstName,
            String lastName,
            LocalDate birthDate,
            UserGender gender,
            String guardianName,
            String guardianPhone,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
    }

    public static Student create(
            Long organizationId,
            String firstName,
            String lastName,
            LocalDate birthDate,
            UserGender gender,
            String guardianName,
            String guardianPhone) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        return new Student(
                null,
                organizationId,
                null,
                firstName,
                lastName,
                birthDate,
                gender,
                guardianName,
                guardianPhone,
                null,
                null,
                null);
    }

    public static Student rehydrate(
            Long id,
            Long organizationId,
            Long userId,
            String firstName,
            String lastName,
            LocalDate birthDate,
            UserGender gender,
            String guardianName,
            String guardianPhone,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new Student(
                id,
                organizationId,
                userId,
                firstName,
                lastName,
                birthDate,
                gender,
                guardianName,
                guardianPhone,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    public void update(
            String firstName,
            String lastName,
            LocalDate birthDate,
            UserGender gender,
            String guardianName,
            String guardianPhone) {
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
    }
}
