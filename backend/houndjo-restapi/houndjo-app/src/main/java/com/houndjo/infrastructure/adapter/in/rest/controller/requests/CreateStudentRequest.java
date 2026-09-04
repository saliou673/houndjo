package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.UserGender;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request to create a student in the active organization.
 *
 * @param firstName     given name
 * @param lastName      family name
 * @param birthDate     optional date of birth
 * @param gender        optional gender
 * @param guardianName  optional guardian name
 * @param guardianPhone optional guardian phone number
 */
public record CreateStudentRequest(
        @NotBlank @Size(max = 255) String firstName,
        @NotBlank @Size(max = 255) String lastName,
        @Nullable LocalDate birthDate,
        @Nullable UserGender gender,
        @Nullable @Size(max = 255) String guardianName,
        @Nullable @Size(max = 20) String guardianPhone) {}
