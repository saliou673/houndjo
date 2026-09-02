package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.UserStatus;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.hibernate.annotations.BatchSize;

/**
 * JPA entity mapping the {@code app_user} table.
 */
@Entity
@Table(name = "app_user")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class UserEntity extends AuditableEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** Surrogate primary key. */
    private Long id;

    @Embedded
    /** Embedded profile information. */
    private EmbeddableUserInfo userInfo;

    @Embedded
    /** Embedded authentication credentials. */
    private EmbeddableCredentials userCredentials;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    /** Current account lifecycle status. */
    private UserStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_role_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_group_id"))
    @BatchSize(size = 20)
    private Set<RoleGroupEntity> roleGroups = new HashSet<>();

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "two_factor_method", length = 50)
    private TwoFactorMethodType twoFactorMethod;

    @Column(name = "totp_secret", length = 255)
    private String totpSecret;

    public boolean isActivated() {
        return status == UserStatus.ACTIVATED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
