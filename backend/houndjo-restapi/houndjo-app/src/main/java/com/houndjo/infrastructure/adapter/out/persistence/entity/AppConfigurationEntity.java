package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;

/**
 * JPA entity mapping the {@code app_configuration} table.
 */
@Entity
@Table(name = "app_configuration", uniqueConstraints = @UniqueConstraint(columnNames = {"category", "code"}))
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class AppConfigurationEntity extends AuditableEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private AppConfigurationCategory category;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "description")
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppConfigurationEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
