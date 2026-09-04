package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO representing a single Mushaf page and the verses it carries.
 *
 * @param number the page number, 1..604
 * @param verses the verses appearing on this page, ordered by surah then verse number
 */
@Schema(name = "Page")
public record PageDTO(int number, List<VerseDTO> verses) {}
