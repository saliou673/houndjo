package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.organization.OrganizationInvitation;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.InvitationDTO;
import org.springframework.stereotype.Component;

@Component
public class InvitationDtoMapper {
    public InvitationDTO toDTO(OrganizationInvitation i) {
        return new InvitationDTO(
                i.getId(), i.getEmail(), i.getRole(), i.getStatus(), i.getExpiresAt(), i.getCreationDate());
    }
}
