package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.houndjo.domain.enumerations.OrganizationStatus;
import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.ports.in.OrganizationUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.integration.IntegrationTest;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

class OrganizationControllerTest extends IntegrationTest {

    private static final String API = "/api/organizations";

    @Autowired
    private OrganizationUseCase organizationUseCase;

    // region register

    @Test
    @WithMockUser
    void shouldRegisterSchoolSuccessfully() throws Exception {
        RegisterSchoolRequest request = new RegisterSchoolRequest(
                "Ecole Al Nour", "contact@al-nour.test", "+224600000000", "Conakry", null, null);

        OrganizationDTO result = post(API + "/register", request, OrganizationDTO.class, status().isCreated());

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
    @WithMockUser
    void shouldHonorExplicitCurrencyAndLanguage() throws Exception {
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("Ecole Ibn Sina", "contact@ibn-sina.test", null, null, "XOF", "en");

        OrganizationDTO result = post(API + "/register", request, OrganizationDTO.class, status().isCreated());

        assertThat(result.getDefaultCurrencyCode()).isEqualTo("XOF");
        assertThat(result.getDefaultLanguageKey()).isEqualTo("en");
    }

    @Test
    @WithMockUser
    void shouldSuffixSlugOnCollision() throws Exception {
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("Ecole Al Nour", "contact1@al-nour.test", null, null, null, null);
        RegisterSchoolRequest duplicateRequest =
                new RegisterSchoolRequest("Ecole Al Nour", "contact2@al-nour.test", null, null, null, null);

        post(API + "/register", request, OrganizationDTO.class, status().isCreated());
        OrganizationDTO second = post(API + "/register", duplicateRequest, OrganizationDTO.class, status().isCreated());

        assertThat(second.getSlug()).isEqualTo("ecole-al-nour-2");
    }

    @Test
    @WithMockUser
    void shouldPreserveUnicodeNamesWhenGeneratingSlug() throws Exception {
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("مدرسة النور", "contact@al-nour.test", null, null, null, null);

        OrganizationDTO result = post(API + "/register", request, OrganizationDTO.class, status().isCreated());

        assertThat(result.getSlug()).isEqualTo("مدرسة-النور");
        assertThat(Organization.slugify("École Al Nour")).isEqualTo("ecole-al-nour");
        assertThat(Organization.slugify("✨")).isEqualTo("school");
    }

    @Test
    void shouldAllocateDistinctSlugsForConcurrentRegistrations() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            Future<Organization> first = executor.submit(() -> registerAfter(start, "first@concurrent.test"));
            Future<Organization> second = executor.submit(() -> registerAfter(start, "second@concurrent.test"));

            start.countDown();

            assertThat(Set.of(first.get().getSlug(), second.get().getSlug()))
                    .containsExactlyInAnyOrder("concurrent-school", "concurrent-school-2");
        }
    }

    @Test
    @WithMockUser
    void shouldFailToRegisterSchoolWithBlankName() throws Exception {
        RegisterSchoolRequest request = new RegisterSchoolRequest("", "contact@al-nour.test", null, null, null, null);

        post(API + "/register", request, status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldFailToRegisterSchoolWithInvalidEmail() throws Exception {
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("Ecole Al Nour", "not-an-email", null, null, null, null);

        post(API + "/register", request, status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldRejectValuesLongerThanDatabaseColumns() throws Exception {
        RegisterSchoolRequest request = new RegisterSchoolRequest(
                "x".repeat(151),
                "contact@al-nour.test",
                "1".repeat(21),
                "a".repeat(256),
                "X".repeat(11),
                "f".repeat(6));

        post(API + "/register", request, status().isBadRequest());
    }

    // endregion

    // region getById

    @Test
    @WithMockUser(authorities = "organization:read")
    void shouldGetOrganizationByIdSuccessfully() throws Exception {
        RegisterSchoolRequest request =
                new RegisterSchoolRequest("Ecole Al Nour", "contact@al-nour.test", null, null, null, null);
        OrganizationDTO created = post(API + "/register", request, OrganizationDTO.class, status().isCreated());

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

    private Organization registerAfter(CountDownLatch start, String email) throws Exception {
        start.await();
        return organizationUseCase.registerSchool(
                Organization.create("Concurrent School", email, null, null, null, null));
    }
}
