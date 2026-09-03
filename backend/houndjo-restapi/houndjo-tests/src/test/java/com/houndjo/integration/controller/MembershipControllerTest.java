package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.MembershipStatus;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.MembershipDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.ChangeMembershipRoleRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class MembershipControllerTest extends IntegrationTest {

    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    // region list

    @Test
    void shouldListMembershipsOfAnOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");

        PaginatedResult<MembershipDTO> result = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + organization.getId() + "/memberships")
                        .with(jwt().authorities(new SimpleGrantedAuthority("membership:read"))),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(1);
        MembershipDTO membership = result.getItems().getFirst();
        assertThat(membership.userEmail()).isEqualTo(OWNER_EMAIL);
        assertThat(membership.role()).isEqualTo(OrganizationRole.SCHOOL_OWNER);
        assertThat(membership.status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.organizationId()).isEqualTo(organization.getId());
    }

    @Test
    void shouldForbidListMembershipsWithoutPermission() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");

        mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + organization.getId() + "/memberships")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // endregion

    // region changeRole

    @Test
    void shouldChangeMembershipRole() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long membershipId = firstMembershipId(organization.getId());

        ChangeMembershipRoleRequest request = new ChangeMembershipRoleRequest(OrganizationRole.SCHOOL_ADMIN);

        MembershipDTO result = mockMvc(
                MockMvcRequestBuilders.patch(ORGANIZATION_API + "/" + organization.getId() + "/memberships/"
                                + membershipId + "/role")
                        .with(jwt().authorities(new SimpleGrantedAuthority("membership:update")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                MembershipDTO.class,
                status().isOk());

        assertThat(result.role()).isEqualTo(OrganizationRole.SCHOOL_ADMIN);
    }

    // endregion

    // region revoke

    @Test
    void shouldRevokeMembership() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long membershipId = firstMembershipId(organization.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete(
                                ORGANIZATION_API + "/" + organization.getId() + "/memberships/" + membershipId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("membership:delete"))))
                .andExpect(status().isNoContent());

        PaginatedResult<MembershipDTO> result = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + organization.getId() + "/memberships")
                        .with(jwt().authorities(new SimpleGrantedAuthority("membership:read"))),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems().getFirst().status()).isEqualTo(MembershipStatus.REVOKED);
    }

    // endregion

    private Long firstMembershipId(Long organizationId) throws Exception {
        PaginatedResult<MembershipDTO> result = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + organizationId + "/memberships")
                        .with(jwt().authorities(new SimpleGrantedAuthority("membership:read"))),
                new TypeReference<>() {},
                status().isOk());
        return result.getItems().getFirst().id();
    }

    private OrganizationDTO registerAsOwner(String email, String name, String contactEmail) throws Exception {
        RegisterSchoolRequest request = new RegisterSchoolRequest(name, contactEmail, null, null, null, null);
        String response = mockMvc.perform(MockMvcRequestBuilders.post(ORGANIZATION_API + "/register")
                        .with(jwt().jwt(j -> j.subject(email)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, OrganizationDTO.class);
    }

    private <T> T mockMvc(MockHttpServletRequestBuilder builder, TypeReference<T> typeReference, ResultMatcher matcher)
            throws Exception {
        String response = mockMvc.perform(builder)
                .andExpect(matcher)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, typeReference);
    }

    private <T> T mockMvc(MockHttpServletRequestBuilder builder, Class<T> responseType, ResultMatcher matcher)
            throws Exception {
        String response = mockMvc.perform(builder)
                .andExpect(matcher)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, responseType);
    }
}
