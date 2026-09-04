package com.houndjo.domain.models.enrollment;

import com.houndjo.domain.enumerations.EnrollmentStatus;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

/**
 * Aggregate linking a student to a class (and the courses taken within it). A student cannot
 * have more than one {@code ACTIVE} enrollment in the same class.
 */
@Getter
public class Enrollment extends Auditable<Long> {

    private final Long organizationId;
    private final Long studentId;
    private final Long classId;
    private final Set<Long> courseIds;
    private EnrollmentStatus status;
    private final LocalDate startDate;
    private LocalDate endDate;

    private Enrollment(
            Long id,
            Long organizationId,
            Long studentId,
            Long classId,
            Set<Long> courseIds,
            EnrollmentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.studentId = studentId;
        this.classId = classId;
        this.courseIds = new HashSet<>(courseIds == null ? Set.of() : courseIds);
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Enrollment create(
            Long organizationId, Long studentId, Long classId, Set<Long> courseIds, LocalDate startDate) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(studentId, "studentId must not be null");
        Objects.requireNonNull(classId, "classId must not be null");
        Objects.requireNonNull(startDate, "startDate must not be null");
        return new Enrollment(
                null,
                organizationId,
                studentId,
                classId,
                courseIds,
                EnrollmentStatus.ACTIVE,
                startDate,
                null,
                null,
                null,
                null);
    }

    public static Enrollment rehydrate(
            Long id,
            Long organizationId,
            Long studentId,
            Long classId,
            Set<Long> courseIds,
            EnrollmentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new Enrollment(
                id,
                organizationId,
                studentId,
                classId,
                courseIds,
                status,
                startDate,
                endDate,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    public Set<Long> getCourseIds() {
        return Set.copyOf(courseIds);
    }

    public void end(LocalDate endDate) {
        Objects.requireNonNull(endDate, "endDate must not be null");
        this.status = EnrollmentStatus.ENDED;
        this.endDate = endDate;
    }

    public void addCourses(Set<Long> courseIdsToAdd) {
        if (courseIdsToAdd != null) {
            this.courseIds.addAll(courseIdsToAdd);
        }
    }

    public void removeCourses(Set<Long> courseIdsToRemove) {
        if (courseIdsToRemove != null) {
            this.courseIds.removeAll(courseIdsToRemove);
        }
    }

    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }
}
