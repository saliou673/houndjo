package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.MembershipStatus;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.ports.out.persistenceport.MembershipPersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.MembershipDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.ChangeMembershipRoleRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class MembershipControllerTest extends IntegrationTest {

    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    @Autowired
    private MembershipPersistencePort membershipPersistencePort;

    // region list

    @Test
    void shouldListMembershipsOfAnOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");

        PaginatedResult<MembershipDTO> result = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + organization.getId() + "/memberships")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "membership:read")),
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
    void shouldForbidListMembershipsWithoutOrgRole() throws Exception {
        // MembershipController is gated purely by @authz.hasOrgRole(...), not by a permission
        // code, so the forbidden case is a user with no active membership in the organization
        // at all -- not merely a missing authority string on an otherwise-valid owner.
        createUser(OWNER_EMAIL);
        String outsiderEmail = "outsider@al-nour.test";
        createUser(outsiderEmail);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");

        mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + organization.getId() + "/memberships")
                        .with(authenticatedForOrganization(outsiderEmail, organization.getId(), "membership:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotListMembershipsOutsideActiveOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");

        mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + otherOrganization.getId() + "/memberships")
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "membership:read")))
                .andExpect(status().isNotFound());
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
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "membership:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                MembershipDTO.class,
                status().isOk());

        assertThat(result.role()).isEqualTo(OrganizationRole.SCHOOL_ADMIN);
    }

    @Test
    void shouldNotChangeMembershipFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        Long otherMembershipId = firstMembershipId(otherOrganization.getId(), otherOwner);
        ChangeMembershipRoleRequest request = new ChangeMembershipRoleRequest(OrganizationRole.SCHOOL_ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.patch(ORGANIZATION_API + "/" + activeOrganization.getId()
                                + "/memberships/" + otherMembershipId + "/role")
                        .with(authenticatedForOrganization(
                                OWNER_EMAIL, activeOrganization.getId(), "membership:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region revoke

    @Test
    void shouldRevokeMembership() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        // Revoke a second member's membership rather than the owner's own: the owner is the
        // organization's only SCHOOL_OWNER, and hasOrgRole() only considers ACTIVE memberships,
        // so revoking it would lock the very actor performing the request out of the org
        // for the follow-up list call below.
        Long teacherId = createUser("teacher@al-nour.test").getId();
        Membership teacherMembership = membershipPersistencePort.save(
                Membership.create(teacherId, organization.getId(), OrganizationRole.TEACHER));

        mockMvc.perform(MockMvcRequestBuilders.delete(ORGANIZATION_API + "/" + organization.getId()
                                + "/memberships/" + teacherMembership.getId())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "membership:delete")))
                .andExpect(status().isNoContent());

        PaginatedResult<MembershipDTO> result = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + organization.getId() + "/memberships")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "membership:read")),
                new TypeReference<>() {},
                status().isOk());

        MembershipDTO revoked = result.getItems().stream()
                .filter(membership -> membership.id().equals(teacherMembership.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(revoked.status()).isEqualTo(MembershipStatus.REVOKED);
    }

    @Test
    void shouldNotRevokeMembershipFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        Long otherMembershipId = firstMembershipId(otherOrganization.getId(), otherOwner);

        mockMvc.perform(MockMvcRequestBuilders.delete(ORGANIZATION_API + "/" + activeOrganization.getId()
                                + "/memberships/" + otherMembershipId)
                        .with(authenticatedForOrganization(
                                OWNER_EMAIL, activeOrganization.getId(), "membership:delete")))
                .andExpect(status().isNotFound());
    }

    // endregion

    private Long firstMembershipId(Long organizationId) throws Exception {
        return firstMembershipId(organizationId, OWNER_EMAIL);
    }

    private Long firstMembershipId(Long organizationId, String email) throws Exception {
        PaginatedResult<MembershipDTO> result = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATION_API + "/" + organizationId + "/memberships")
                        .with(authenticatedForOrganization(email, organizationId, "membership:read")),
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

    private RequestPostProcessor authenticatedForOrganization(String email, Long organizationId, String authority) {
        return jwt().jwt(j -> j.subject(email).claim("orgId", organizationId))
                .authorities(new SimpleGrantedAuthority(authority));
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
