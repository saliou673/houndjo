package com.houndjo.domain.models.academic;

import com.houndjo.domain.enumerations.CourseType;
import java.util.Objects;

/**
 * Tracking config for a {@code BOOK} course.
 *
 * @param bookTitle      the book's title
 * @param totalChapters  optional total chapter count
 * @param totalPages     optional total page count
 */
public record BookTrackingConfig(String bookTitle, Integer totalChapters, Integer totalPages)
        implements TrackingConfig {

    public BookTrackingConfig {
        Objects.requireNonNull(bookTitle, "bookTitle must not be null");
    }

    @Override
    public CourseType type() {
        return CourseType.BOOK;
    }
}
