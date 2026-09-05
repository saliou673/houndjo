package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import com.houndjo.domain.models.progress.ProgressFilter;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.query.PagedResult;

/**
 * Use case for recording and reviewing per-session, per-student progress within the active
 * organization.
 */
public interface ProgressUseCase {

    /**
     * Returns the progress records of the active organization matching the filter, paginated.
     *
     * @param filter search criteria
     * @param page   zero-based page index
     * @param size   maximum items per page
     * @return paginated progress records
     */
    PagedResult<ProgressRecord> findAll(ProgressFilter filter, int page, int size);

    /**
     * Returns a progress record by its identifier within the active organization.
     *
     * @param id the progress record identifier
     * @return the matching progress record
     */
    ProgressRecord getById(Long id);

    /**
     * Records a new progress entry for a (student, session) pair on a given flow.
     *
     * @param studentId  the recorded student identifier
     * @param courseId   the owning course identifier
     * @param sessionId  the session the progress was recorded during
     * @param flow       the tracking flow
     * @param fromSurah  Quran portion start surah, required for Quran flows
     * @param fromVerse  Quran portion start verse, required for Quran flows
     * @param toSurah    Quran portion end surah, required for Quran flows
     * @param toVerse    Quran portion end verse, required for Quran flows
     * @param lessonId   QAIDA lesson identifier, required for the {@code LESSON} flow
     * @param chapterNo  book chapter number, required for the {@code CHAPTER} flow
     * @param pageNo     book page number, required for the {@code CHAPTER} flow
     * @param errorCount number of errors made, >= 0
     * @param fluency    fluency assessment
     * @param tajweed    optional tajweed assessment
     * @param status     validation status
     * @param note       optional free-text note
     * @return the recorded progress record
     */
    ProgressRecord record(
            Long studentId,
            Long courseId,
            Long sessionId,
            ProgressFlow flow,
            Integer fromSurah,
            Integer fromVerse,
            Integer toSurah,
            Integer toVerse,
            Long lessonId,
            Integer chapterNo,
            Integer pageNo,
            int errorCount,
            FluencyRating fluency,
            FluencyRating tajweed,
            ProgressStatus status,
            String note);

    /**
     * Corrects an existing progress record's portion and assessment. The student, course,
     * session and flow are immutable.
     *
     * @param id         the progress record identifier
     * @param fromSurah  Quran portion start surah, required for Quran flows
     * @param fromVerse  Quran portion start verse, required for Quran flows
     * @param toSurah    Quran portion end surah, required for Quran flows
     * @param toVerse    Quran portion end verse, required for Quran flows
     * @param lessonId   QAIDA lesson identifier, required for the {@code LESSON} flow
     * @param chapterNo  book chapter number, required for the {@code CHAPTER} flow
     * @param pageNo     book page number, required for the {@code CHAPTER} flow
     * @param errorCount number of errors made, >= 0
     * @param fluency    fluency assessment
     * @param tajweed    optional tajweed assessment
     * @param status     validation status
     * @param note       optional free-text note
     * @return the updated progress record
     */
    ProgressRecord update(
            Long id,
            Integer fromSurah,
            Integer fromVerse,
            Integer toSurah,
            Integer toVerse,
            Long lessonId,
            Integer chapterNo,
            Integer pageNo,
            int errorCount,
            FluencyRating fluency,
            FluencyRating tajweed,
            ProgressStatus status,
            String note);

    /**
     * Deletes a progress record of the active organization.
     *
     * @param id the progress record identifier
     */
    void delete(Long id);
}
