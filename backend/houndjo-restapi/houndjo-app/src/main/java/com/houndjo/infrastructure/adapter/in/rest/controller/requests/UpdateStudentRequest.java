package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.UserGender;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request to update a student of the active organization.
 *
 * @param firstName     new given name
 * @param lastName      new family name
 * @param birthDate     new date of birth
 * @param gender        new gender
 * @param guardianName  new guardian name
 * @param guardianPhone new guardian phone number
 */
public record UpdateStudentRequest(
        @NotBlank @Size(max = 255) String firstName,
        @NotBlank @Size(max = 255) String lastName,
        @Nullable LocalDate birthDate,
        @Nullable UserGender gender,
        @Nullable @Size(max = 255) String guardianName,
        @Nullable @Size(max = 20) String guardianPhone) {}
