package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.OrganizationStatus;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;

/**
 * JPA entity mapping the {@code organization} table.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "organization", uniqueConstraints = @UniqueConstraint(columnNames = {"slug"}))
public class OrganizationEntity extends AuditableEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "slug", nullable = false, length = 160)
    private String slug;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "default_currency_code", nullable = false, length = 10)
    private String defaultCurrencyCode;

    @Column(name = "default_language_key", nullable = false, length = 5)
    private String defaultLanguageKey;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
