package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request to generate recurring sessions for a course.
 *
 * @param fromDate first day of the generation range, inclusive
 * @param toDate   last day of the generation range, inclusive
 */
public record GenerateSessionsRequest(
        @NotNull LocalDate fromDate, @NotNull LocalDate toDate) {}
