package com.houndjo.domain.models.academic;

import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate representing a course attached to a {@link SchoolClass}. Course typology is
 * extensible: the type-specific tracking rules live entirely in {@link TrackingConfig}, never as
 * hardcoded per-type logic here.
 * <p>
 * The course name is a single string in the input language for the MVP — per-locale
 * multilingual naming is deferred to V1.
 */
@Getter
public class Course extends Auditable<Long> {

    private final Long organizationId;
    private final Long classId;
    private String name;
    private String description;
    private TrackingConfig trackingConfig;

    private Course(
            Long id,
            Long organizationId,
            Long classId,
            String name,
            String description,
            TrackingConfig trackingConfig,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.classId = classId;
        this.name = name;
        this.description = description;
        this.trackingConfig = trackingConfig;
    }

    public static Course create(
            Long organizationId, Long classId, String name, String description, TrackingConfig trackingConfig) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(classId, "classId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(trackingConfig, "trackingConfig must not be null");
        return new Course(null, organizationId, classId, name, description, trackingConfig, null, null, null);
    }

    public static Course rehydrate(
            Long id,
            Long organizationId,
            Long classId,
            String name,
            String description,
            TrackingConfig trackingConfig,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new Course(
                id,
                organizationId,
                classId,
                name,
                description,
                trackingConfig,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    public void update(String name, String description, TrackingConfig trackingConfig) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(trackingConfig, "trackingConfig must not be null");
        this.name = name;
        this.description = description;
        this.trackingConfig = trackingConfig;
    }

    public CourseType getType() {
        return trackingConfig.type();
    }
}
