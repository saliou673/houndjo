package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.FeeType;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.FeeScheduleDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateFeeScheduleRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateFeeScheduleRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class FeeScheduleControllerTest extends IntegrationTest {

    private static final String FEE_SCHEDULES_API = "/api/v1/fee-schedules";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    // region create

    @Test
    void shouldCreateFeeScheduleWithExplicitCurrency() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateFeeScheduleRequest request =
                new CreateFeeScheduleRequest(FeeType.TUITION_MONTHLY, new BigDecimal("50000.00"), "XOF", "Monthly tuition");

        FeeScheduleDTO result = mockMvc(
                MockMvcRequestBuilders.post(FEE_SCHEDULES_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "billing:manage"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                FeeScheduleDTO.class,
                status().isCreated());

        assertThat(result.id()).isNotNull();
        assertThat(result.type()).isEqualTo(FeeType.TUITION_MONTHLY);
        assertThat(result.amount()).isEqualByComparingTo("50000.00");
        assertThat(result.currencyCode()).isEqualTo("XOF");
        assertThat(result.label()).isEqualTo("Monthly tuition");
        assertThat(result.active()).isTrue();
    }

    @Test
    void shouldInheritOrganizationDefaultCurrencyWhenNotProvided() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateFeeScheduleRequest request =
                new CreateFeeScheduleRequest(FeeType.REGISTRATION, new BigDecimal("10000.00"), null, "Registration fee");

        FeeScheduleDTO result = mockMvc(
                MockMvcRequestBuilders.post(FEE_SCHEDULES_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "billing:manage"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                FeeScheduleDTO.class,
                status().isCreated());

        assertThat(result.currencyCode()).isEqualTo(organization.getDefaultCurrencyCode());
    }

    @Test
    void shouldRejectCreateWithNonPositiveAmount() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateFeeScheduleRequest request =
                new CreateFeeScheduleRequest(FeeType.REGISTRATION, BigDecimal.ZERO, null, "Registration fee");

        mockMvc.perform(MockMvcRequestBuilders.post(FEE_SCHEDULES_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "billing:manage"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldForbidCreateWithoutOrgRole() throws Exception {
        String noRoleEmail = "outsider@al-nour.test";
        createUser(OWNER_EMAIL);
        createUser(noRoleEmail);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateFeeScheduleRequest request =
                new CreateFeeScheduleRequest(FeeType.REGISTRATION, new BigDecimal("10000.00"), null, "Registration fee");

        mockMvc.perform(MockMvcRequestBuilders.post(FEE_SCHEDULES_API)
                        .with(authenticatedForOrganization(noRoleEmail, organization.getId(), "billing:manage"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // endregion

    // region list & get

    @Test
    void shouldListFeeSchedulesOfActiveOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        createFeeSchedule(OWNER_EMAIL, organization.getId(), FeeType.REGISTRATION, "Registration fee");
        createFeeSchedule(OWNER_EMAIL, organization.getId(), FeeType.TUITION_MONTHLY, "Monthly tuition");

        PaginatedResult<FeeScheduleDTO> result = mockMvc(
                MockMvcRequestBuilders.get(FEE_SCHEDULES_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "billing:manage")),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(2);
    }

    @Test
    void shouldGetFeeScheduleById() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        FeeScheduleDTO created =
                createFeeSchedule(OWNER_EMAIL, organization.getId(), FeeType.REGISTRATION, "Registration fee");

        FeeScheduleDTO result = mockMvc(
                MockMvcRequestBuilders.get(FEE_SCHEDULES_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "billing:manage")),
                FeeScheduleDTO.class,
                status().isOk());

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.label()).isEqualTo("Registration fee");
    }

    @Test
    void shouldNotGetFeeScheduleFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        FeeScheduleDTO otherFeeSchedule =
                createFeeSchedule(otherOwner, otherOrganization.getId(), FeeType.REGISTRATION, "Registration fee");

        mockMvc.perform(MockMvcRequestBuilders.get(FEE_SCHEDULES_API + "/" + otherFeeSchedule.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "billing:manage")))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region update

    @Test
    void shouldUpdateFeeSchedule() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        FeeScheduleDTO created =
                createFeeSchedule(OWNER_EMAIL, organization.getId(), FeeType.REGISTRATION, "Registration fee");
        UpdateFeeScheduleRequest request = new UpdateFeeScheduleRequest(
                FeeType.TUITION_ANNUAL, new BigDecimal("300000.00"), "XOF", "Annual tuition", false);

        FeeScheduleDTO result = mockMvc(
                MockMvcRequestBuilders.put(FEE_SCHEDULES_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "billing:manage"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                FeeScheduleDTO.class,
                status().isOk());

        assertThat(result.type()).isEqualTo(FeeType.TUITION_ANNUAL);
        assertThat(result.amount()).isEqualByComparingTo("300000.00");
        assertThat(result.currencyCode()).isEqualTo("XOF");
        assertThat(result.label()).isEqualTo("Annual tuition");
        assertThat(result.active()).isFalse();
    }

    @Test
    void shouldNotUpdateFeeScheduleFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        FeeScheduleDTO otherFeeSchedule =
                createFeeSchedule(otherOwner, otherOrganization.getId(), FeeType.REGISTRATION, "Registration fee");
        UpdateFeeScheduleRequest request =
                new UpdateFeeScheduleRequest(FeeType.REGISTRATION, new BigDecimal("1.00"), null, "Hacked", true);

        mockMvc.perform(MockMvcRequestBuilders.put(FEE_SCHEDULES_API + "/" + otherFeeSchedule.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "billing:manage"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region delete

    @Test
    void shouldDeleteFeeSchedule() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        FeeScheduleDTO created =
                createFeeSchedule(OWNER_EMAIL, organization.getId(), FeeType.REGISTRATION, "Registration fee");

        mockMvc.perform(MockMvcRequestBuilders.delete(FEE_SCHEDULES_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "billing:manage")))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get(FEE_SCHEDULES_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "billing:manage")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteFeeScheduleFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        FeeScheduleDTO otherFeeSchedule =
                createFeeSchedule(otherOwner, otherOrganization.getId(), FeeType.REGISTRATION, "Registration fee");

        mockMvc.perform(MockMvcRequestBuilders.delete(FEE_SCHEDULES_API + "/" + otherFeeSchedule.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "billing:manage")))
                .andExpect(status().isNotFound());
    }

    // endregion

    private FeeScheduleDTO createFeeSchedule(String email, Long organizationId, FeeType type, String label)
            throws Exception {
        CreateFeeScheduleRequest request = new CreateFeeScheduleRequest(type, new BigDecimal("10000.00"), null, label);
        return mockMvc(
                MockMvcRequestBuilders.post(FEE_SCHEDULES_API)
                        .with(authenticatedForOrganization(email, organizationId, "billing:manage"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                FeeScheduleDTO.class,
                status().isCreated());
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
