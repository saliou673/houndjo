package com.houndjo.domain.models.academic;

import com.houndjo.domain.enumerations.CourseType;
import java.util.List;

/**
 * Tracking config for a {@code QAIDA} course.
 *
 * @param lessons the ordered lesson names used by the curriculum
 */
public record QaidaTrackingConfig(List<String> lessons) implements TrackingConfig {

    public QaidaTrackingConfig {
        if (lessons == null || lessons.isEmpty()) {
            throw new IllegalArgumentException("lessons must not be empty");
        }
        if (lessons.stream().anyMatch(lesson -> lesson == null || lesson.isBlank())) {
            throw new IllegalArgumentException("lessons must not contain blank values");
        }
        lessons = List.copyOf(lessons);
    }

    @Override
    public CourseType type() {
        return CourseType.QAIDA;
    }
}
