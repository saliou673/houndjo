package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.UserGender;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import lombok.*;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
/** Embeddable JPA component for user profile information. */
public class EmbeddableUserInfo {
    @Column(name = "first_name", nullable = false)
    /** Given name. */
    private String firstName;

    @Column(name = "last_name", nullable = false)
    /** Family name. */
    private String lastName;

    @Column(name = "phone_number")
    /** Optional phone number. */
    private String phoneNumber;

    @Column(name = "birth_date", nullable = false)
    /** Date of birth. */
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    /** Biological gender. */
    private UserGender gender;

    @Column(name = "address")
    /** Optional postal address. */
    private String address;

    @Column(name = "language_key")
    /** Preferred locale key. */
    private String languageKey;

    @Column(name = "image_url")
    /** Profile picture URL. */
    private String imageUrl;
}
