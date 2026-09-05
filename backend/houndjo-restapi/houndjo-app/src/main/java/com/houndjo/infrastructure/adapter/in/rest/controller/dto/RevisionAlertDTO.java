package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressStateDTO.StaleDhorPortionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO representing one (student, course) pair with at least one stale Dhor portion, for
 * the organization-wide revision-alerts dashboard aggregate.
 *
 * @param studentId     the student identifier
 * @param courseId      the owning QURAN course identifier
 * @param stalePortions the student's stale Dhor portions for that course
 */
@Schema(name = "RevisionAlert")
public record RevisionAlertDTO(Long studentId, Long courseId, List<StaleDhorPortionDTO> stalePortions) {}
