package com.houndjo.domain.models.progress;

import com.houndjo.domain.enumerations.ProgressFlow;

/**
 * Polymorphic pointer to the portion worked on in one {@link ProgressRecord}. Each
 * {@link ProgressFlow} group has exactly one variant: {@link QuranPortionRef} for the Quran
 * flows ({@code SABAK}/{@code SABQI}/{@code DHOR}), {@link LessonPortionRef} for {@code LESSON},
 * {@link ChapterPortionRef} for {@code CHAPTER}.
 */
public sealed interface PortionRef permits QuranPortionRef, LessonPortionRef, ChapterPortionRef {}
