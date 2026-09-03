package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.in.AccountUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.MembershipDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Membership} to {@link MembershipDTO}, enriching it with the member's email and
 * full name looked up via {@link AccountUseCase}.
 */
@Component
@RequiredArgsConstructor
public class MembershipDtoMapper {

    private final AccountUseCase accountUseCase;

    public MembershipDTO toDTO(Membership membership) {
        User user = accountUseCase.getUserWithAuthoritiesById(membership.getUserId());
        return new MembershipDTO(
                membership.getId(),
                membership.getUserId(),
                user.getUserCredentials().getEmail(),
                user.getUserInfo().firstName() + " " + user.getUserInfo().lastName(),
                membership.getOrganizationId(),
                membership.getRole(),
                membership.getStatus(),
                membership.getCreationDate());
    }
}
