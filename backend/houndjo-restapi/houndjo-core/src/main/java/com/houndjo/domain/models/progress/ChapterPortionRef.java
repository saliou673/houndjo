package com.houndjo.domain.models.progress;

/**
 * The book chapter/page worked on for the {@code CHAPTER} flow.
 *
 * @param chapterNo the chapter number within the course's {@code BookTrackingConfig}
 * @param pageNo    the page number reached within the chapter
 */
public record ChapterPortionRef(int chapterNo, int pageNo) implements PortionRef {}
