package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.MembershipStatus;
import com.houndjo.domain.enumerations.OrganizationRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Response DTO representing an organization membership, enriched with the member's email and
 * full name.
 *
 * @param id             membership identifier
 * @param userId         the member's user identifier
 * @param userEmail      the member's email
 * @param userFullName   the member's full name
 * @param organizationId the organization identifier
 * @param role           the member's organization-scoped role
 * @param status         the membership status
 * @param creationDate   when the membership was created
 */
@Schema(name = "Membership")
public record MembershipDTO(
        Long id,
        Long userId,
        String userEmail,
        String userFullName,
        Long organizationId,
        OrganizationRole role,
        MembershipStatus status,
        Instant creationDate) {}
