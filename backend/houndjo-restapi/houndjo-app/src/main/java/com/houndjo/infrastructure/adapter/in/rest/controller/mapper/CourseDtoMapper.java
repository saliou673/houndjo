package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.academic.BookTrackingConfig;
import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.academic.QaidaTrackingConfig;
import com.houndjo.domain.models.academic.QuranTrackingConfig;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO.QuranScopeDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Course} to {@link CourseDTO}, populating only the fields relevant to its type.
 */
@Component
public class CourseDtoMapper {

    public CourseDTO toDTO(Course course) {
        return switch (course.getTrackingConfig()) {
            case QuranTrackingConfig quran ->
                new CourseDTO(
                        course.getId(),
                        course.getClassId(),
                        course.getName(),
                        course.getType(),
                        course.getDescription(),
                        null,
                        quran.mode(),
                        new QuranScopeDTO(quran.fromJuz(), quran.toJuz()),
                        null,
                        null,
                        null,
                        course.getCreationDate());
            case QaidaTrackingConfig qaida ->
                new CourseDTO(
                        course.getId(),
                        course.getClassId(),
                        course.getName(),
                        course.getType(),
                        course.getDescription(),
                        qaida.lessons(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        course.getCreationDate());
            case BookTrackingConfig book ->
                new CourseDTO(
                        course.getId(),
                        course.getClassId(),
                        course.getName(),
                        course.getType(),
                        course.getDescription(),
                        null,
                        null,
                        null,
                        book.bookTitle(),
                        book.totalChapters(),
                        book.totalPages(),
                        course.getCreationDate());
        };
    }
}
