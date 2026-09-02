package com.houndjo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base abstract class for entities which will hold definitions for creation, last modified, last modified by attributes.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class AuditableEntity<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public abstract T getId();

    @CreatedDate
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Instant creationDate;

    @LastModifiedDate
    @Column(name = "last_update_date", nullable = false)
    private Instant lastUpdateDate;

    @LastModifiedBy
    @Column(name = "last_updated_by", nullable = false)
    private String lastUpdatedBy;
}
