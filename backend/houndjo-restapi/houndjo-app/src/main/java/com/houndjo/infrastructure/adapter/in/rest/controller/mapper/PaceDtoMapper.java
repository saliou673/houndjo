package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.pace.CoursePace;
import com.houndjo.domain.models.pace.PaceFlow;
import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PaceDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PaceFlowDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PortionDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link CoursePace} to {@link PaceDTO}, and {@link QuranPortion} to {@link PortionDTO}.
 */
@Component
public class PaceDtoMapper {

    public PaceDTO toDTO(CoursePace coursePace) {
        return new PaceDTO(
                coursePace.getCourseId(),
                coursePace.getUnit(),
                coursePace.getAmountPerSession(),
                coursePace.getSessionsPerWeek(),
                toFlowDTO(coursePace.getSabak()),
                toFlowDTO(coursePace.getSabqi()),
                toFlowDTO(coursePace.getDhor()),
                coursePace.getDhorCycleDays());
    }

    public PortionDTO toDTO(QuranPortion portion) {
        return new PortionDTO(
                portion.fromSurah(),
                portion.fromVerse(),
                portion.toSurah(),
                portion.toVerse(),
                portion.fromPage(),
                portion.toPage(),
                portion.fromJuz(),
                portion.toJuz(),
                portion.fromHizb(),
                portion.toHizb());
    }

    private PaceFlowDTO toFlowDTO(PaceFlow flow) {
        return flow == null ? null : new PaceFlowDTO(flow.unit(), flow.amount());
    }
}
