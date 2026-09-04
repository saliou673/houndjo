package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Response DTO representing a school class.
 *
 * @param id           class identifier
 * @param name         display name
 * @param description  optional description
 * @param displayOrder ordering hint among the organization's classes
 * @param courseCount  number of courses attached to this class
 * @param creationDate when the class was created
 */
@Schema(name = "Class")
public record ClassDTO(
        Long id, String name, String description, int displayOrder, int courseCount, Instant creationDate) {}
