package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.academic.BookTrackingConfig;
import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.academic.QaidaTrackingConfig;
import com.houndjo.domain.models.academic.QuranTrackingConfig;
import com.houndjo.domain.models.academic.TrackingConfig;
import com.houndjo.infrastructure.adapter.out.persistence.entity.CourseEntity;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link CourseEntity}'s flat, type-specific columns and the polymorphic
 * {@link TrackingConfig} domain hierarchy. Kept as a plain mapper (not MapStruct) since the
 * mapping is conditional on {@code type} rather than a straight property copy.
 */
@Component
public class CourseMapper {

    public Course toDomain(CourseEntity entity) {
        if (entity == null) {
            return null;
        }
        return Course.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getClassId(),
                entity.getName(),
                entity.getDescription(),
                toTrackingConfig(entity),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    public List<Course> toDomain(List<CourseEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }

    public CourseEntity toEntity(Course course) {
        CourseEntity entity = new CourseEntity(
                course.getId(),
                course.getOrganizationId(),
                course.getClassId(),
                course.getName(),
                course.getType(),
                course.getDescription(),
                null,
                null,
                null,
                null,
                null,
                null);
        applyTrackingConfig(entity, course.getTrackingConfig());
        return entity;
    }

    private TrackingConfig toTrackingConfig(CourseEntity entity) {
        return switch (entity.getType()) {
            case QURAN -> new QuranTrackingConfig(
                    entity.getQuranMode(),
                    entity.getQuranScopeFromJuz().intValue(),
                    entity.getQuranScopeToJuz().intValue());
            case QAIDA -> new QaidaTrackingConfig();
            case BOOK -> new BookTrackingConfig(
                    entity.getBookTitle(),
                    entity.getBookTotalChapters() == null
                            ? null
                            : entity.getBookTotalChapters().intValue(),
                    entity.getBookTotalPages() == null
                            ? null
                            : entity.getBookTotalPages().intValue());
        };
    }

    private void applyTrackingConfig(CourseEntity entity, TrackingConfig trackingConfig) {
        switch (trackingConfig) {
            case QuranTrackingConfig quran -> {
                entity.setQuranMode(quran.mode());
                entity.setQuranScopeFromJuz((short) quran.fromJuz());
                entity.setQuranScopeToJuz((short) quran.toJuz());
            }
            case QaidaTrackingConfig ignored -> {
                // no type-specific columns yet
            }
            case BookTrackingConfig book -> {
                entity.setBookTitle(book.bookTitle());
                entity.setBookTotalChapters(
                        book.totalChapters() == null ? null : book.totalChapters().shortValue());
                entity.setBookTotalPages(book.totalPages() == null ? null : book.totalPages().shortValue());
            }
        }
    }
}
