package com.houndjo.domain.models;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Base class carrying audit metadata for all domain entities.
 *
 * @param <T> the type of the entity identifier
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Auditable<T> {
    /**
     * Unique entity identifier.
     */
    private T id;
    /**
     * Timestamp when the entity was first created.
     */
    private Instant creationDate;
    /**
     * Timestamp of the last modification.
     */
    private Instant lastUpdateDate;
    /**
     * Email of the user who last modified the entity.
     */
    private String lastUpdatedBy;
}
