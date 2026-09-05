package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO for a student's attendance history and resulting absence rate.
 *
 * @param entries     the matching attendance entries, in session date order
 * @param absenceRate fraction of entries not marked {@code PRESENT}, between 0 and 1
 */
@Schema(name = "AttendanceHistory")
public record AttendanceHistoryDTO(List<AttendanceDTO> entries, double absenceRate) {}
