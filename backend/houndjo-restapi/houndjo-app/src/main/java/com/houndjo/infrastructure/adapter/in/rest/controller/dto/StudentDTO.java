package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO representing a student. {@code userId} is intentionally not exposed at MVP.
 *
 * @param id             student identifier
 * @param firstName      given name
 * @param lastName       family name
 * @param birthDate      optional date of birth
 * @param gender         optional gender
 * @param guardianName   optional guardian name
 * @param guardianPhone  optional guardian phone number
 * @param creationDate   when the student was created
 */
@Schema(name = "Student")
public record StudentDTO(
        Long id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        UserGender gender,
        String guardianName,
        String guardianPhone,
        Instant creationDate) {}
