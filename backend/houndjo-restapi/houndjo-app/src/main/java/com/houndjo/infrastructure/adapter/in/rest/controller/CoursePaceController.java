package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.enumerations.QuranFlow;
import com.houndjo.domain.models.pace.PaceFlow;
import com.houndjo.domain.ports.in.CoursePaceUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PaceDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PortionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.PaceDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.SetPaceRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing a course's target pace within the active organization.
 */
@Validated
@RestController
@Tag(name = "Course pace")
@PreAuthorize("hasAuthority('course:read')")
@RequestMapping(path = "/api/v1/courses/{courseId}/pace", version = "1.0")
@RequiredArgsConstructor
public class CoursePaceController {

    private final CoursePaceUseCase coursePaceUseCase;
    private final PaceDtoMapper paceDtoMapper;

    @GetMapping
    public PaceDTO getPace(@PathVariable Long courseId) {
        return paceDtoMapper.toDTO(coursePaceUseCase.getPace(courseId));
    }

    @GetMapping("/next-portion")
    public PortionDTO getNextPortion(
            @PathVariable Long courseId, @RequestParam Long studentId, @RequestParam QuranFlow flow) {
        return paceDtoMapper.toDTO(coursePaceUseCase.getNextPortion(courseId, studentId, flow));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('course:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public PaceDTO setPace(@PathVariable Long courseId, @Valid @RequestBody SetPaceRequest request) {
        return paceDtoMapper.toDTO(coursePaceUseCase.setPace(
                courseId,
                request.unit(),
                request.amountPerSession(),
                request.sessionsPerWeek(),
                toFlow(request.sabak()),
                toFlow(request.sabqi()),
                toFlow(request.dhor()),
                request.dhorCycleDays()));
    }

    private PaceFlow toFlow(SetPaceRequest.FlowRequest flowRequest) {
        return flowRequest == null ? null : new PaceFlow(flowRequest.unit(), flowRequest.amount());
    }
}
