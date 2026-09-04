package com.houndjo.domain.enumerations;

/**
 * Lifecycle status of an {@link com.houndjo.domain.models.enrollment.Enrollment}.
 */
public enum EnrollmentStatus {
    /**
     * The student is currently enrolled in the class.
     */
    ACTIVE,
    /**
     * The enrollment has ended.
     */
    ENDED
}
