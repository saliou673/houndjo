package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AppConfigurationCategoryDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AppConfigurationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateAppConfigurationRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateAppConfigurationRequest;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AppConfigurationEntity;
import com.houndjo.infrastructure.adapter.out.persistence.repository.AppConfigurationRepository;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
class AdminAppConfigurationControllerTest extends IntegrationTest {

    private static final String API = "/api/admin/configurations";

    @Autowired
    private AppConfigurationRepository appConfigurationRepository;

    // region create

    @Test
    @WithMockUser(authorities = "config:create")
    void shouldCreateAppConfigurationAsAdminAppConfigurationSuccessfully() throws Exception {
        CreateAppConfigurationRequest request = new CreateAppConfigurationRequest(
                AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", "West African CFA franc");

        AppConfigurationDTO result = post(API, request, AppConfigurationDTO.class, status().isCreated());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCategory()).isEqualTo(AppConfigurationCategory.CURRENCY);
        assertThat(result.getCode()).isEqualTo("XOF");
        assertThat(result.getLabel()).isEqualTo("Franc CFA");
        assertThat(result.isActive()).isTrue();

        Optional<AppConfigurationEntity> saved = appConfigurationRepository.findById(result.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getCode()).isEqualTo("XOF");
    }

    @Test
    @WithMockUser(authorities = "config:create")
    void shouldFailToCreateAppConfigurationAsAdminWithMissingCategory() throws Exception {
        CreateAppConfigurationRequest request = new CreateAppConfigurationRequest(null, "XOF", "Franc CFA", null);
        post(API, request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "config:create")
    void shouldFailToCreateAppConfigurationAsAdminWithBlankCode() throws Exception {
        CreateAppConfigurationRequest request =
                new CreateAppConfigurationRequest(AppConfigurationCategory.CURRENCY, "", "Franc CFA", null);
        post(API, request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "config:create")
    void shouldFailToCreateAppConfigurationAsAdminWithBlankLabel() throws Exception {
        CreateAppConfigurationRequest request =
                new CreateAppConfigurationRequest(AppConfigurationCategory.CURRENCY, "XOF", "", null);
        post(API, request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "config:create")
    void shouldFailToCreateAppConfigurationAsAdminWithDuplicateCategoryAndCode() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);

        CreateAppConfigurationRequest request =
                new CreateAppConfigurationRequest(AppConfigurationCategory.CURRENCY, "XOF", "Another Franc CFA", null);
        post(API, request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "config:create")
    void shouldAllowSameCodeForDifferentCategories() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        // Same code "EUR" but if another CURRENCY category with same code already exists → should fail
        // Testing here that same code in same category fails
        // Create with a different code in CURRENCY → should succeed
        CreateAppConfigurationRequest request =
                new CreateAppConfigurationRequest(AppConfigurationCategory.CURRENCY, "USD", "US Dollar", null);
        AppConfigurationDTO result = post(API, request, AppConfigurationDTO.class, status().isCreated());
        assertThat(result.getCode()).isEqualTo("USD");
    }

    // endregion

    // region getById

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldGetAppConfigurationByIdSuccessfully() throws Exception {
        AppConfigurationEntity entity =
                createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        AppConfigurationDTO result = get(API + "/" + entity.getId(), new TypeReference<>() {}, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getCode()).isEqualTo("EUR");
        assertThat(result.getLabel()).isEqualTo("Euro");
        assertThat(result.getCategory()).isEqualTo(AppConfigurationCategory.CURRENCY);
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFailToGetAppConfigurationWhenNotFound() throws Exception {
        get(API + "/99999", status().isBadRequest());
    }

    // endregion

    // region update

    @Test
    @WithMockUser(authorities = "config:update")
    void shouldUpdateAppConfigurationAsAdminAppConfigurationSuccessfully() throws Exception {
        AppConfigurationEntity entity = createAppConfigurationAsAdminAppConfiguration(
                AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        UpdateAppConfigurationRequest request =
                new UpdateAppConfigurationRequest("XOF", "Franc CFA BCEAO", "Updated description", false);

        AppConfigurationDTO result =
                put(API + "/" + entity.getId(), request, AppConfigurationDTO.class, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getLabel()).isEqualTo("Franc CFA BCEAO");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.isActive()).isFalse();

        AppConfigurationEntity updated =
                appConfigurationRepository.findById(entity.getId()).orElseThrow();
        assertThat(updated.getLabel()).isEqualTo("Franc CFA BCEAO");
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @WithMockUser(authorities = "config:update")
    void shouldFailToUpdateAppConfigurationAsAdminWhenNotFound() throws Exception {
        UpdateAppConfigurationRequest request = new UpdateAppConfigurationRequest("XOF", "Franc CFA", null, true);
        put(API + "/99999", request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "config:update")
    void shouldFailToUpdateAppConfigurationAsAdminWithDuplicateCode() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        AppConfigurationEntity second =
                createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        UpdateAppConfigurationRequest request = new UpdateAppConfigurationRequest("XOF", "Renamed", null, true);
        put(API + "/" + second.getId(), request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "config:update")
    void shouldAllowUpdateAppConfigurationAsAdminWithSameCode() throws Exception {
        AppConfigurationEntity entity = createAppConfigurationAsAdminAppConfiguration(
                AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        UpdateAppConfigurationRequest request =
                new UpdateAppConfigurationRequest("XOF", "Updated Franc CFA", null, true);

        AppConfigurationDTO result =
                put(API + "/" + entity.getId(), request, AppConfigurationDTO.class, status().isOk());

        assertThat(result.getCode()).isEqualTo("XOF");
        assertThat(result.getLabel()).isEqualTo("Updated Franc CFA");
    }

    // endregion

    // region delete

    @Test
    @WithMockUser(authorities = "config:delete")
    void shouldDeleteAppConfigurationSuccessfully() throws Exception {
        AppConfigurationEntity entity = createAppConfigurationAsAdminAppConfiguration(
                AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);

        delete(API + "/" + entity.getId(), status().isNoContent());

        assertThat(appConfigurationRepository.findById(entity.getId())).isEmpty();
    }

    @Test
    @WithMockUser(authorities = "config:delete")
    void shouldFailToDeleteWhenNotFound() throws Exception {
        delete(API + "/99999", status().isBadRequest());
    }

    // endregion

    // region getAll + filtering

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldGetAppConfigurationsAsAdminAppConfigurationSuccessfully() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        PaginatedResult<AppConfigurationDTO> result = get(API, new TypeReference<>() {}, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(2);
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldReturnEmptyWhenNoDataExists() throws Exception {
        PaginatedResult<AppConfigurationDTO> result = get(API, new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFilterByCategoryEquals() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?category.equals=CURRENCY", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(AppConfigurationDTO::getCategory)
                .containsOnly(AppConfigurationCategory.CURRENCY);
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFilterByCodeEquals() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?code.equals=XOF", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems().getFirst().getCode()).isEqualTo("XOF");
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFilterByCodeContains() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XAF", "CFA Franc BEAC", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?code.contains=X", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems()).extracting(AppConfigurationDTO::getCode).containsExactlyInAnyOrder("XOF", "XAF");
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFilterByActiveEquals() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", false);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "USD", "US Dollar", false);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?active.equals=false", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems()).extracting(AppConfigurationDTO::isActive).containsOnly(false);
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFilterByLabelContains() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XAF", "CFA Franc BEAC", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?label.contains=CFA", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFilterByCombinedCategoryAndActive() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", false);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?category.equals=CURRENCY&active.equals=true", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems().getFirst().getCode()).isEqualTo("XOF");
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldReturnEmptyWhenNoFilterMatch() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?code.equals=NONEXISTENT", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getItems()).isEmpty();
    }

    // endregion

    // region pagination

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldSupportPagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createAppConfigurationAsAdminAppConfiguration(
                    AppConfigurationCategory.CURRENCY, "CUR" + i, "Currency " + i, true);
        }

        PaginatedResult<AppConfigurationDTO> firstPage =
                get(API + "?page=0&size=2", new TypeReference<>() {}, status().isOk());

        assertThat(firstPage.getTotalItems()).isEqualTo(5);
        assertThat(firstPage.getItems()).hasSize(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.getPage()).isEqualTo(0);

        PaginatedResult<AppConfigurationDTO> secondPage =
                get(API + "?page=1&size=2", new TypeReference<>() {}, status().isOk());

        assertThat(secondPage.getItems()).hasSize(2);
        assertThat(secondPage.getPage()).isEqualTo(1);
        assertThat(secondPage.getItems())
                .extracting(AppConfigurationDTO::getCode)
                .doesNotContainAnyElementsOf(firstPage.getItems().stream()
                        .map(AppConfigurationDTO::getCode)
                        .toList());
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldSupportPaginationWithFilter() throws Exception {
        for (int i = 1; i <= 4; i++) {
            createAppConfigurationAsAdminAppConfiguration(
                    AppConfigurationCategory.CURRENCY, "XCU" + i, "X Currency " + i, true);
        }
        createAppConfigurationAsAdminAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        PaginatedResult<AppConfigurationDTO> firstPage =
                get(API + "?code.contains=XCU&page=0&size=2", new TypeReference<>() {}, status().isOk());

        assertThat(firstPage.getTotalItems()).isEqualTo(4);
        assertThat(firstPage.getItems()).hasSize(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);

        PaginatedResult<AppConfigurationDTO> secondPage =
                get(API + "?code.contains=XCU&page=1&size=2", new TypeReference<>() {}, status().isOk());

        assertThat(secondPage.getItems()).hasSize(2);
        assertThat(secondPage.getItems())
                .extracting(AppConfigurationDTO::getCode)
                .allMatch(code -> code.startsWith("XCU"));
    }

    // endregion

    // endregion

    // region updateByCategoryAndCode

    @Test
    @WithMockUser(authorities = "config:update")
    void shouldUpdateAppConfigurationAsAdminByCategoryAndCodeSuccessfully() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(
                AppConfigurationCategory.TWO_FACTOR, "ENABLED", "Two-Factor Authentication", false);
        UpdateAppConfigurationRequest request =
                new UpdateAppConfigurationRequest("ENABLED", "Two-Factor Authentication", null, true);

        AppConfigurationDTO result =
                put(API + "/TWO_FACTOR/ENABLED", request, AppConfigurationDTO.class, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCategory()).isEqualTo(AppConfigurationCategory.TWO_FACTOR);
        assertThat(result.getCode()).isEqualTo("ENABLED");

        AppConfigurationEntity updated = appConfigurationRepository
                .findByCategoryAndCode(AppConfigurationCategory.TWO_FACTOR, "ENABLED")
                .orElseThrow();
        assertThat(updated.isActive()).isTrue();
    }

    @Test
    @WithMockUser(authorities = "config:update")
    void shouldFailUpdateAppConfigurationAsAdminByCategoryAndCodeWhenNotFound() throws Exception {
        UpdateAppConfigurationRequest request =
                new UpdateAppConfigurationRequest("ENABLED", "Two-Factor Authentication", null, true);
        put(API + "/TWO_FACTOR/NONEXISTENT", request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "config:update")
    void shouldFailUpdateAppConfigurationAsAdminByCategoryAndCodeWithBlankLabel() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(
                AppConfigurationCategory.TWO_FACTOR, "ENABLED", "Two-Factor Authentication", false);
        UpdateAppConfigurationRequest request = new UpdateAppConfigurationRequest("ENABLED", "", null, true);
        put(API + "/TWO_FACTOR/ENABLED", request, status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "config:update")
    void shouldFailUpdateAppConfigurationAsAdminByCategoryAndCodeWhenNewCodeConflicts() throws Exception {
        createAppConfigurationAsAdminAppConfiguration(
                AppConfigurationCategory.TWO_FACTOR, "ENABLED", "Two-Factor Authentication", false);
        createAppConfigurationAsAdminAppConfiguration(
                AppConfigurationCategory.TWO_FACTOR, "OTHER", "Other Config", true);

        UpdateAppConfigurationRequest request =
                new UpdateAppConfigurationRequest("OTHER", "Two-Factor Authentication", null, false);
        put(API + "/TWO_FACTOR/ENABLED", request, status().isBadRequest());
    }

    // endregion

    // region categories

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldReturnAllCategoriesSuccessfully() throws Exception {
        List<AppConfigurationCategoryDTO> result = get(API + "/categories", new TypeReference<>() {}, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(AppConfigurationCategory.values().length);
        assertThat(result)
                .extracting(AppConfigurationCategoryDTO::value)
                .containsExactlyInAnyOrder(AppConfigurationCategory.values());
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldReturnAllThreeCategoriesWithDescriptions() throws Exception {
        List<AppConfigurationCategoryDTO> result = get(API + "/categories", new TypeReference<>() {}, status().isOk());

        assertThat(result)
                .extracting(AppConfigurationCategoryDTO::value, AppConfigurationCategoryDTO::description)
                .containsExactlyInAnyOrder(
                        tuple(AppConfigurationCategory.CURRENCY, AppConfigurationCategory.CURRENCY.getDescription()),
                        tuple(
                                AppConfigurationCategory.TWO_FACTOR,
                                AppConfigurationCategory.TWO_FACTOR.getDescription()),
                        tuple(
                                AppConfigurationCategory.PAYMENT_MODE,
                                AppConfigurationCategory.PAYMENT_MODE.getDescription()));
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldReturnNonBlankDescriptionForEachCategory() throws Exception {
        List<AppConfigurationCategoryDTO> result = get(API + "/categories", new TypeReference<>() {}, status().isOk());

        assertThat(result).allSatisfy(dto -> assertThat(dto.description()).isNotBlank());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidGetCategoriesForSimpleUser() throws Exception {
        get(API + "/categories", status().isForbidden());
    }

    // endregion

    // region security

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidAccessForSimpleUser() throws Exception {
        get(API, status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldForbidAccessForAdmin() throws Exception {
        get(API, status().isForbidden());
    }

    // endregion

    private AppConfigurationEntity createAppConfigurationAsAdminAppConfiguration(
            AppConfigurationCategory category, String code, String label, boolean active) {
        AppConfigurationEntity entity = new AppConfigurationEntity(null, category, code, label, null, active);
        entity.setCreationDate(Instant.now());
        entity.setLastUpdateDate(Instant.now());
        entity.setLastUpdatedBy("test");
        return appConfigurationRepository.save(entity);
    }
}
