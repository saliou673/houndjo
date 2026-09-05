package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.ports.in.ProgressStateUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressStateDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.ProgressStateDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing a student's computed progress state for a course.
 */
@Validated
@RestController
@Tag(name = "Progress tracking")
@PreAuthorize("hasAuthority('progress:read')")
@RequestMapping(path = "/api/v1/students/{studentId}/progress-state", version = "1.0")
@RequiredArgsConstructor
public class ProgressStateController {

    private final ProgressStateUseCase progressStateUseCase;
    private final ProgressStateDtoMapper progressStateDtoMapper;

    @GetMapping
    public ProgressStateDTO getProgressState(@PathVariable Long studentId, @RequestParam Long courseId) {
        return progressStateDtoMapper.toDTO(progressStateUseCase.getProgressState(studentId, courseId));
    }
}
