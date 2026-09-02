package com.houndjo.domain.models.user;

import com.houndjo.domain.enumerations.UserGender;
import java.time.LocalDate;

/**
 * Command carrying the fields a user may update on their own profile.
 *
 * @param firstName   new given name
 * @param lastName    new family name
 * @param phoneNumber new phone number (optional)
 * @param birthDate   new birthdate (optional, keeps current value when omitted)
 * @param gender      new gender (optional, keeps current value when omitted)
 * @param address     new postal address (optional)
 * @param languageKey new preferred locale key (optional)
 * @param imageUrl    new profile picture URL (optional)
 */
public record UserInfoUpdate(
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate birthDate,
        UserGender gender,
        String address,
        String languageKey,
        String imageUrl) {}
