package com.houndjo.domain.models.attendance;

import java.util.List;

/**
 * A student's attendance history over a date range, together with the resulting absence rate
 * (share of entries where the student was not marked {@code PRESENT}, including approved
 * leave recorded as {@code PERMISSION}). This measures missed sessions, not disciplinary fault.
 *
 * @param entries      the matching attendance entries, in session date order
 * @param absenceRate  fraction of entries not marked {@code PRESENT}, between 0 and 1 (0 when
 *                      there are no entries)
 */
public record StudentAttendanceHistory(List<Attendance> entries, double absenceRate) {}
