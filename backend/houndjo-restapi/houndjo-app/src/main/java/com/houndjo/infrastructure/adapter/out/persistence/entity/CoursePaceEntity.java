package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.PaceUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity mapping the {@code course_pace} table.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "course_pace")
public class CoursePaceEntity extends AuditableEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 15)
    private PaceUnit unit;

    @Column(name = "amount_per_session", nullable = false)
    private BigDecimal amountPerSession;

    @Column(name = "sessions_per_week", nullable = false)
    private int sessionsPerWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "sabak_unit", length = 15)
    private PaceUnit sabakUnit;

    @Column(name = "sabak_amount")
    private BigDecimal sabakAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "sabqi_unit", length = 15)
    private PaceUnit sabqiUnit;

    @Column(name = "sabqi_amount")
    private BigDecimal sabqiAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "dhor_unit", length = 15)
    private PaceUnit dhorUnit;

    @Column(name = "dhor_amount")
    private BigDecimal dhorAmount;

    @Column(name = "dhor_cycle_days")
    private Integer dhorCycleDays;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoursePaceEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
