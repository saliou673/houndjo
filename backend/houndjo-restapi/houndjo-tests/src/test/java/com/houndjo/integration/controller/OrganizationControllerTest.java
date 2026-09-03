package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.OrganizationStatus;
import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.ports.in.OrganizationUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateOrganizationRequest;
import com.houndjo.integration.IntegrationTest;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class OrganizationControllerTest extends IntegrationTest {

    private static final String API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    @Autowired
    private OrganizationUseCase organizationUseCase;

    // region register

    @Test
    void shouldRegisterSchoolSuccessfully() throws Exception {
        createUser(OWNER_EMAIL);
        RegisterSchoolRequest request = new RegisterSchoolRequest(
                "Ecole Al Nour", "contact@al-nour.test", "+224600000000", "Conakry", null, null);

        OrganizationDTO result = registerAsOwner(OWNER_EMAIL, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Ecole Al Nour");
        assertThat(result.getSlug()).isEqualTo("ecole-al-nour");
        assertThat(result.getContactEmail()).isEqualTo("contact@al-nour.test");
        assertThat(result.getPhoneNumber()).isEqualTo("+224600000000");
        assertThat(result.getAddress()).isEqualTo("Conakry");
        assertThat(result.getDefaultCurrencyCode()).isEqualTo("GNF");
        assertThat(result.getDefaultLanguageKey()).isEqualTo("fr");
        assertThat(result.getTimezone()).isEqualTo("Africa/Conakry");
        assertThat(result.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
    }

    @Test
    void shouldHonorExplicitCurrencyAndLanguage() throws Exception {
        createUser(OWNER_EMAIL);
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("Ecole Ibn Sina", "contact@ibn-sina.test", null, null, "XOF", "en");

        OrganizationDTO result = registerAsOwner(OWNER_EMAIL, request);

        assertThat(result.getDefaultCurrencyCode()).isEqualTo("XOF");
        assertThat(result.getDefaultLanguageKey()).isEqualTo("en");
    }

    @Test
    void shouldSuffixSlugOnCollision() throws Exception {
        createUser(OWNER_EMAIL);
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("Ecole Al Nour", "contact1@al-nour.test", null, null, null, null);
        RegisterSchoolRequest duplicateRequest =
                new RegisterSchoolRequest("Ecole Al Nour", "contact2@al-nour.test", null, null, null, null);

        registerAsOwner(OWNER_EMAIL, request);
        OrganizationDTO second = registerAsOwner(OWNER_EMAIL, duplicateRequest);

        assertThat(second.getSlug()).isEqualTo("ecole-al-nour-2");
    }

    @Test
    void shouldPreserveUnicodeNamesWhenGeneratingSlug() throws Exception {
        createUser(OWNER_EMAIL);
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("مدرسة النور", "contact@al-nour.test", null, null, null, null);

        OrganizationDTO result = registerAsOwner(OWNER_EMAIL, request);

        assertThat(result.getSlug()).isEqualTo("مدرسة-النور");
        assertThat(Organization.slugify("École Al Nour")).isEqualTo("ecole-al-nour");
        assertThat(Organization.slugify("✨")).isEqualTo("school");
    }

    @Test
    void shouldAllocateDistinctSlugsForConcurrentRegistrations() throws Exception {
        Long firstUserId = createUser("first@concurrent.test").getId();
        Long secondUserId = createUser("second@concurrent.test").getId();
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            Future<Organization> first = executor.submit(() -> registerAfter(start, firstUserId));
            Future<Organization> second = executor.submit(() -> registerAfter(start, secondUserId));

            start.countDown();

            assertThat(Set.of(first.get().getSlug(), second.get().getSlug()))
                    .containsExactlyInAnyOrder("concurrent-school", "concurrent-school-2");
        }
    }

    @Test
    void shouldFailToRegisterSchoolWithBlankName() throws Exception {
        createUser(OWNER_EMAIL);
        RegisterSchoolRequest request = new RegisterSchoolRequest("", "contact@al-nour.test", null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(API + "/register")
                        .with(authenticatedAs(OWNER_EMAIL))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToRegisterSchoolWithInvalidEmail() throws Exception {
        createUser(OWNER_EMAIL);
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("Ecole Al Nour", "not-an-email", null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(API + "/register")
                        .with(authenticatedAs(OWNER_EMAIL))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectValuesLongerThanDatabaseColumns() throws Exception {
        createUser(OWNER_EMAIL);
        RegisterSchoolRequest request = new RegisterSchoolRequest(
                "x".repeat(151),
                "contact@al-nour.test",
                "1".repeat(21),
                "a".repeat(256),
                "X".repeat(11),
                "f".repeat(6));

        mockMvc.perform(MockMvcRequestBuilders.post(API + "/register")
                        .with(authenticatedAs(OWNER_EMAIL))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // endregion

    // region mine

    @Test
    void shouldOnlyListOrganizationsTheUserIsAMemberOf() throws Exception {
        createUser(OWNER_EMAIL);
        String otherEmail = "owner2@ibn-sina.test";
        createUser(otherEmail);

        registerAsOwner(
                OWNER_EMAIL, new RegisterSchoolRequest("Ecole Al Nour", "a@al-nour.test", null, null, null, null));
        registerAsOwner(
                otherEmail, new RegisterSchoolRequest("Ecole Ibn Sina", "b@ibn-sina.test", null, null, null, null));

        String response = mockMvc.perform(
                        MockMvcRequestBuilders.get(API + "/mine").with(authenticatedAs(OWNER_EMAIL)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<OrganizationDTO> result = objectMapper.readValue(response, new TypeReference<>() {});

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Ecole Al Nour");
    }

    // endregion

    // region getById

    @Test
    @WithMockUser(authorities = "organization:read")
    void shouldGetOrganizationByIdSuccessfully() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO created = registerAsOwner(
                OWNER_EMAIL,
                new RegisterSchoolRequest("Ecole Al Nour", "contact@al-nour.test", null, null, null, null));

        OrganizationDTO result = get(API + "/" + created.getId(), OrganizationDTO.class, status().isOk());

        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getName()).isEqualTo("Ecole Al Nour");
    }

    @Test
    @WithMockUser(authorities = "organization:read")
    void shouldFailToGetOrganizationWhenNotFound() throws Exception {
        get(API + "/99999", status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldForbidGetOrganizationForUserWithoutPermission() throws Exception {
        get(API + "/1", status().isForbidden());
    }

    // endregion

    // region update

    @Test
    void shouldUpdateOrganizationAsOwner() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO created = registerAsOwner(
                OWNER_EMAIL,
                new RegisterSchoolRequest("Ecole Al Nour", "contact@al-nour.test", null, null, null, null));

        UpdateOrganizationRequest updateRequest = new UpdateOrganizationRequest(
                "Ecole Al Nour Renamed", "new-contact@al-nour.test", null, null, null, null, null);

        String response = mockMvc.perform(MockMvcRequestBuilders.put(API + "/" + created.getId())
                        .with(jwt().jwt(j -> j.subject(OWNER_EMAIL).claim("orgId", created.getId()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        OrganizationDTO updated = objectMapper.readValue(response, OrganizationDTO.class);

        assertThat(updated.getName()).isEqualTo("Ecole Al Nour Renamed");
        assertThat(updated.getContactEmail()).isEqualTo("new-contact@al-nour.test");
    }

    @Test
    void shouldForbidUpdateForNonMember() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO created = registerAsOwner(
                OWNER_EMAIL,
                new RegisterSchoolRequest("Ecole Al Nour", "contact@al-nour.test", null, null, null, null));

        String otherEmail = "not-a-member@test.com";
        createUser(otherEmail);
        UpdateOrganizationRequest updateRequest =
                new UpdateOrganizationRequest("Hacked Name", "contact@al-nour.test", null, null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.put(API + "/" + created.getId())
                        .with(jwt().jwt(j -> j.subject(otherEmail).claim("orgId", created.getId()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUpdateWhenPathIdDiffersFromActiveOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO activeOrg = registerAsOwner(
                OWNER_EMAIL,
                new RegisterSchoolRequest("Ecole Al Nour", "contact@al-nour.test", null, null, null, null));
        OrganizationDTO otherOrg = registerAsOwner(
                OWNER_EMAIL,
                new RegisterSchoolRequest("Ecole Ibn Sina", "contact@ibn-sina.test", null, null, null, null));

        // owner of both orgs, but the active org (JWT claim) is activeOrg while the path targets otherOrg
        UpdateOrganizationRequest updateRequest =
                new UpdateOrganizationRequest("Hacked Name", "contact@ibn-sina.test", null, null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.put(API + "/" + otherOrg.getId())
                        .with(jwt().jwt(j -> j.subject(OWNER_EMAIL).claim("orgId", activeOrg.getId()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    // endregion

    private OrganizationDTO registerAsOwner(String email, RegisterSchoolRequest request) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post(API + "/register")
                        .with(authenticatedAs(email))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, OrganizationDTO.class);
    }

    private RequestPostProcessor authenticatedAs(String email) {
        return jwt().jwt(j -> j.subject(email)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private Organization registerAfter(CountDownLatch start, Long creatorUserId) throws Exception {
        start.await();
        return organizationUseCase.registerSchool(
                Organization.create(
                        "Concurrent School", "contact@concurrent.test", null, null, null, null),
                creatorUserId);
    }
}
