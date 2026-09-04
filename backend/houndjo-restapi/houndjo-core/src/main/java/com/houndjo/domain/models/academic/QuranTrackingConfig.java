package com.houndjo.domain.models.academic;

import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.QuranMode;
import com.houndjo.domain.exceptions.InvalidQuranScopeException;
import java.util.Objects;

/**
 * Tracking config for a {@code QURAN} course: a target juz range plus reading/memorization mode.
 * Existence of {@code fromJuz}/{@code toJuz} against the Quran reference data (E2) is verified by
 * the application layer, which has access to {@code QuranReferencePort}; this record only
 * enforces the structural invariant that the range is not inverted.
 *
 * @param mode    reading (NAZIRA) or memorization (HIFZ)
 * @param fromJuz the first juz of the target range, 1..30
 * @param toJuz   the last juz of the target range, 1..30
 */
public record QuranTrackingConfig(QuranMode mode, int fromJuz, int toJuz) implements TrackingConfig {

    public QuranTrackingConfig {
        Objects.requireNonNull(mode, "mode must not be null");
        if (fromJuz > toJuz) {
            throw new InvalidQuranScopeException(fromJuz, toJuz);
        }
    }

    @Override
    public CourseType type() {
        return CourseType.QURAN;
    }
}
