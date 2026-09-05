package com.houndjo.domain.models.progress;

import java.util.Objects;

/**
 * The QAIDA curriculum lesson worked on for the {@code LESSON} flow.
 *
 * @param lessonId zero-based index of the lesson within the course's {@code QaidaTrackingConfig}
 */
public record LessonPortionRef(Long lessonId) implements PortionRef {

    public LessonPortionRef {
        Objects.requireNonNull(lessonId, "lessonId must not be null");
    }
}
