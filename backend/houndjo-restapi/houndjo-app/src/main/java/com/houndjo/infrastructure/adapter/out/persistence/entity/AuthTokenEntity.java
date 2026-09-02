package com.houndjo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * JPA entity mapping the {@code auth_token} table.
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "auth_token")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class AuthTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    @Column(name = "remember_me", nullable = false)
    private Boolean rememberMe;

    @Column(nullable = false)
    private Instant expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @CreatedDate
    @Column(name = "creation_date", updatable = false, nullable = false)
    private Instant creationDate = Instant.now();
}
