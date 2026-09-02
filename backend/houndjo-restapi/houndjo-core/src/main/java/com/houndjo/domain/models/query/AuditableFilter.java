package com.houndjo.domain.models.query;

import com.houndjo.domain.models.query.filter.InstantFilter;
import com.houndjo.domain.models.query.filter.StringFilter;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;

/**
 * Base filter exposing audit-field criteria shared by all entity filters.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class AuditableFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Filter on the creation timestamp.
     */
    private InstantFilter creationDate;
    /**
     * Filter on the last-modification timestamp.
     */
    private InstantFilter lastUpdateDate;
    /**
     * Filter on the email of the last modifier.
     */
    private StringFilter lastUpdatedBy;
}
