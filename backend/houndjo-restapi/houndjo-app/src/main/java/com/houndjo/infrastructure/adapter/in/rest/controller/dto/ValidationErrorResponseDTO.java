package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error response returned when bean-validation fails on an incoming request.
 *
 * @param timestamp the time at which the error occurred
 * @param status    the HTTP status code
 * @param message   a human-readable summary message
 * @param errors    map of field name to validation error message
 */
public record ValidationErrorResponseDTO(
        LocalDateTime timestamp, int status, String message, Map<String, String> errors) {}
