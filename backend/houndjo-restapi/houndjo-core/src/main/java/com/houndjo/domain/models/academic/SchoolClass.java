package com.houndjo.domain.models.academic;

import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate representing a grade/class level within an organization (e.g. "CP1", "Groupe Hifz A").
 */
@Getter
public class SchoolClass extends Auditable<Long> {

    private final Long organizationId;
    private String name;
    private String description;
    private int displayOrder;

    private SchoolClass(
            Long id,
            Long organizationId,
            String name,
            String description,
            int displayOrder,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public static SchoolClass create(Long organizationId, String name, String description, int displayOrder) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        return new SchoolClass(null, organizationId, name, description, displayOrder, null, null, null);
    }

    public static SchoolClass rehydrate(
            Long id,
            Long organizationId,
            String name,
            String description,
            int displayOrder,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new SchoolClass(
                id, organizationId, name, description, displayOrder, creationDate, lastUpdateDate, lastUpdatedBy);
    }

    public void update(String name, String description, int displayOrder) {
        Objects.requireNonNull(name, "name must not be null");
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }
}
