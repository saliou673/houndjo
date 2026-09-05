package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.progress.FlowSnapshot;
import com.houndjo.domain.models.progress.ProgressState;
import com.houndjo.domain.models.progress.RevisionAlert;
import com.houndjo.domain.models.progress.StaleDhorPortion;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressStateDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressStateDTO.FlowSnapshotDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressStateDTO.StaleDhorPortionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.RevisionAlertDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link ProgressState}/{@link RevisionAlert} to their response DTOs.
 */
@Component
public class ProgressStateDtoMapper {

    public ProgressStateDTO toDTO(ProgressState state) {
        var stalePortions = state.stalePortions().stream().map(this::toDTO).toList();
        return new ProgressStateDTO(
                toDTO(state.sabak()), toDTO(state.sabqi()), state.coveredJuz(), stalePortions, stalePortions);
    }

    public RevisionAlertDTO toDTO(RevisionAlert alert) {
        return new RevisionAlertDTO(
                alert.studentId(),
                alert.courseId(),
                alert.stalePortions().stream().map(this::toDTO).toList());
    }

    private FlowSnapshotDTO toDTO(FlowSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new FlowSnapshotDTO(
                snapshot.portion().fromSurah(),
                snapshot.portion().fromVerse(),
                snapshot.portion().toSurah(),
                snapshot.portion().toVerse(),
                snapshot.date());
    }

    private StaleDhorPortionDTO toDTO(StaleDhorPortion portion) {
        return new StaleDhorPortionDTO(portion.juz(), portion.lastReviewedDate(), portion.daysSince());
    }
}
