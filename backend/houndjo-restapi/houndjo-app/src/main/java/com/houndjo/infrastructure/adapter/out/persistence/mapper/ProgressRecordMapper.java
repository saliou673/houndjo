package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.progress.ChapterPortionRef;
import com.houndjo.domain.models.progress.LessonPortionRef;
import com.houndjo.domain.models.progress.PortionRef;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.progress.QuranPortionRef;
import com.houndjo.infrastructure.adapter.out.persistence.entity.ProgressRecordEntity;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link ProgressRecordEntity}'s flat, flow-specific portion columns and the
 * polymorphic {@link PortionRef} domain hierarchy. Kept as a plain mapper (not MapStruct) since
 * the mapping is conditional on {@code flow}/portion type rather than a straight property copy.
 */
@Component
public class ProgressRecordMapper {

    public ProgressRecord toDomain(ProgressRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProgressRecord.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getStudentId(),
                entity.getCourseId(),
                entity.getSessionId(),
                entity.getFlow(),
                toPortion(entity),
                entity.getErrorCount(),
                entity.getFluency(),
                entity.getTajweed(),
                entity.getStatus(),
                entity.getNote(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    public List<ProgressRecord> toDomain(List<ProgressRecordEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }

    public ProgressRecordEntity toEntity(ProgressRecord progressRecord) {
        ProgressRecordEntity entity = new ProgressRecordEntity(
                progressRecord.getId(),
                progressRecord.getOrganizationId(),
                progressRecord.getStudentId(),
                progressRecord.getCourseId(),
                progressRecord.getSessionId(),
                progressRecord.getFlow(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                progressRecord.getErrorCount(),
                progressRecord.getFluency(),
                progressRecord.getTajweed(),
                progressRecord.getStatus(),
                progressRecord.getNote());
        applyPortion(entity, progressRecord.getPortion());
        return entity;
    }

    private PortionRef toPortion(ProgressRecordEntity entity) {
        return switch (entity.getFlow()) {
            case SABAK, SABQI, DHOR ->
                new QuranPortionRef(
                        entity.getFromSurah().intValue(),
                        entity.getFromVerse().intValue(),
                        entity.getToSurah().intValue(),
                        entity.getToVerse().intValue());
            case LESSON -> new LessonPortionRef(entity.getLessonId());
            case CHAPTER ->
                new ChapterPortionRef(
                        entity.getChapterNo().intValue(), entity.getPageNo().intValue());
        };
    }

    private void applyPortion(ProgressRecordEntity entity, PortionRef portion) {
        switch (portion) {
            case QuranPortionRef quran -> {
                entity.setFromSurah((short) quran.fromSurah());
                entity.setFromVerse((short) quran.fromVerse());
                entity.setToSurah((short) quran.toSurah());
                entity.setToVerse((short) quran.toVerse());
            }
            case LessonPortionRef lesson -> entity.setLessonId(lesson.lessonId());
            case ChapterPortionRef chapter -> {
                entity.setChapterNo((short) chapter.chapterNo());
                entity.setPageNo((short) chapter.pageNo());
            }
        }
    }
}
