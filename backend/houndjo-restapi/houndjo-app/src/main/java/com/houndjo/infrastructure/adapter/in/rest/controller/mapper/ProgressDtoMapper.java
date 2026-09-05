package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.progress.ChapterPortionRef;
import com.houndjo.domain.models.progress.LessonPortionRef;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.progress.QuranPortionRef;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressDTO.QuranPortionDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link ProgressRecord} to {@link ProgressDTO}, populating only the portion fields
 * relevant to its flow.
 */
@Component
public class ProgressDtoMapper {

    public ProgressDTO toDTO(ProgressRecord progressRecord) {
        return switch (progressRecord.getPortion()) {
            case QuranPortionRef quran ->
                new ProgressDTO(
                        progressRecord.getId(),
                        progressRecord.getStudentId(),
                        progressRecord.getCourseId(),
                        progressRecord.getSessionId(),
                        progressRecord.getFlow(),
                        new QuranPortionDTO(quran.fromSurah(), quran.fromVerse(), quran.toSurah(), quran.toVerse()),
                        null,
                        null,
                        null,
                        progressRecord.getErrorCount(),
                        progressRecord.getFluency(),
                        progressRecord.getTajweed(),
                        progressRecord.getStatus(),
                        progressRecord.getNote(),
                        progressRecord.getCreationDate());
            case LessonPortionRef lesson ->
                new ProgressDTO(
                        progressRecord.getId(),
                        progressRecord.getStudentId(),
                        progressRecord.getCourseId(),
                        progressRecord.getSessionId(),
                        progressRecord.getFlow(),
                        null,
                        lesson.lessonId(),
                        null,
                        null,
                        progressRecord.getErrorCount(),
                        progressRecord.getFluency(),
                        progressRecord.getTajweed(),
                        progressRecord.getStatus(),
                        progressRecord.getNote(),
                        progressRecord.getCreationDate());
            case ChapterPortionRef chapter ->
                new ProgressDTO(
                        progressRecord.getId(),
                        progressRecord.getStudentId(),
                        progressRecord.getCourseId(),
                        progressRecord.getSessionId(),
                        progressRecord.getFlow(),
                        null,
                        null,
                        chapter.chapterNo(),
                        chapter.pageNo(),
                        progressRecord.getErrorCount(),
                        progressRecord.getFluency(),
                        progressRecord.getTajweed(),
                        progressRecord.getStatus(),
                        progressRecord.getNote(),
                        progressRecord.getCreationDate());
        };
    }
}
