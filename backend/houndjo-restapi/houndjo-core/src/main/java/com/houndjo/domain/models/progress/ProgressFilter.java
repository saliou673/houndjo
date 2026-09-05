package com.houndjo.domain.models.progress;

import com.houndjo.domain.enumerations.ProgressFlow;
import java.time.LocalDate;

/**
 * Filter for listing progress records within the active organization. Null fields mean "no
 * constraint". {@code fromDate}/{@code toDate} bound the record's creation date.
 *
 * @param studentId optional recorded student identifier
 * @param courseId  optional owning course identifier
 * @param flow      optional tracking flow
 * @param fromDate  earliest creation date, inclusive
 * @param toDate    latest creation date, inclusive
 */
public record ProgressFilter(Long studentId, Long courseId, ProgressFlow flow, LocalDate fromDate, LocalDate toDate) {}
