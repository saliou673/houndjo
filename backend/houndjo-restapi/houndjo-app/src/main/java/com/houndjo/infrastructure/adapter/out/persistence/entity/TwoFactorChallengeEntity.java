package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.models.auth.TwoFactorChallengePurpose;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/**
 * JPA entity mapping the {@code two_factor_challenge} table.
 */
@Entity
@Table(name = "two_factor_challenge")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class TwoFactorChallengeEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private TwoFactorMethodType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 20)
    private TwoFactorChallengePurpose purpose;

    @Column(name = "remember_me", nullable = false)
    private boolean rememberMe;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;
}
