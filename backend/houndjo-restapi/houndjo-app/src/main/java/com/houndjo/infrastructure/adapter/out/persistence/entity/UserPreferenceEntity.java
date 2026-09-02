package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.models.userpreference.UserPreferences;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity mapping the {@code user_preference} table.
 */
@Entity
@Table(name = "user_preference")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class UserPreferenceEntity extends AuditableEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", nullable = false, columnDefinition = "jsonb")
    private UserPreferences preferences;

    @Override
    public Long getId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPreferenceEntity other)) return false;
        return userId != null && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
