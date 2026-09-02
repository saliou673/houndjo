package com.houndjo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;

/**
 * JPA entity mapping the {@code app_security_settings} table (singleton row).
 */
@Entity
@Table(name = "app_security_settings")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class SecuritySettingsEntity extends AuditableEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "two_factor_required", nullable = false)
    private boolean twoFactorRequired;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecuritySettingsEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
