package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request to record roll call for a session in one shot.
 *
 * @param entries the roll-call entries, one per student
 */
public record BulkAttendanceRequest(@NotEmpty @Valid List<AttendanceEntryRequest> entries) {}
