package com.houndjo.domain.models.user;

import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.domain.models.DomainValidation;
import java.time.LocalDate;

/**
 * Immutable value object holding a user's personal profile information.
 *
 * @param firstName   given name (required)
 * @param lastName    family name (required)
 * @param phoneNumber optional phone number
 * @param birthDate   date of birth
 * @param gender      biological gender
 * @param address     postal address
 * @param languageKey preferred locale key (e.g. {@code "fr"})
 * @param imageUrl    profile picture URL
 */
public record UserInfo(
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate birthDate,
        UserGender gender,
        String address,
        String languageKey,
        String imageUrl) {

    public UserInfo {
        DomainValidation.checkRequiredField(firstName, "firstName");
        DomainValidation.checkRequiredField(lastName, "lastName");
    }

    UserInfo updateInfo(UserInfoUpdate update) {
        return new UserInfo(
                update.firstName(),
                update.lastName(),
                update.phoneNumber(),
                update.birthDate() != null ? update.birthDate() : birthDate,
                update.gender() != null ? update.gender() : gender,
                update.address(),
                update.languageKey(),
                update.imageUrl());
    }
}
