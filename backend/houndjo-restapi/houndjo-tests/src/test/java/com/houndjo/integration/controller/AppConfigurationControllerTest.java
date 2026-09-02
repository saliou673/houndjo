package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AppConfigurationDTO;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AppConfigurationEntity;
import com.houndjo.infrastructure.adapter.out.persistence.repository.AppConfigurationRepository;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
class AppConfigurationControllerTest extends IntegrationTest {

    private static final String API = "/api/configurations";

    @Autowired
    private AppConfigurationRepository appConfigurationRepository;

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldGetAppConfigurationsAppConfigurationForAuthenticatedUser() throws Exception {
        createAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

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
    void shouldFilterByActiveEqualsTrue() throws Exception {
        createAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", false);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?active.equals=true", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems().getFirst().getCode()).isEqualTo("XOF");
        assertThat(result.getItems().getFirst().isActive()).isTrue();
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFilterByCategoryEquals() throws Exception {
        createAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", true);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?category.equals=CURRENCY", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems())
                .extracting(AppConfigurationDTO::getCategory)
                .containsOnly(AppConfigurationCategory.CURRENCY);
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldFilterByCombinedCategoryAndActive() throws Exception {
        createAppConfiguration(AppConfigurationCategory.CURRENCY, "XOF", "Franc CFA", true);
        createAppConfiguration(AppConfigurationCategory.CURRENCY, "EUR", "Euro", false);

        PaginatedResult<AppConfigurationDTO> result =
                get(API + "?category.equals=CURRENCY&active.equals=true", new TypeReference<>() {}, status().isOk());

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems().getFirst().getCode()).isEqualTo("XOF");
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        get(API, status().isUnauthorized());
    }

    // endregion

    // region getByCategoryAndCode

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldGetByCategoryAndCodeSuccessfully() throws Exception {
        createAppConfiguration(AppConfigurationCategory.TWO_FACTOR, "ENABLED", "Two-Factor Authentication", false);

        AppConfigurationDTO result = get(API + "/TWO_FACTOR/ENABLED", new TypeReference<>() {}, status().isOk());

        assertThat(result).isNotNull();
        assertThat(result.getCategory()).isEqualTo(AppConfigurationCategory.TWO_FACTOR);
        assertThat(result.getCode()).isEqualTo("ENABLED");
        assertThat(result.getLabel()).isEqualTo("Two-Factor Authentication");
        assertThat(result.isActive()).isFalse();
    }

    @Test
    @WithMockUser(authorities = "config:read")
    void shouldReturnNotFoundWhenConfigDoesNotExist() throws Exception {
        get(API + "/TWO_FACTOR/NONEXISTENT", status().isNotFound());
    }

    @Test
    void shouldRequireAuthenticationForGetByCategoryAndCode() throws Exception {
        get(API + "/TWO_FACTOR/ENABLED", status().isUnauthorized());
    }

    // endregion

    private AppConfigurationEntity createAppConfiguration(
            AppConfigurationCategory category, String code, String label, boolean active) {
        AppConfigurationEntity entity = new AppConfigurationEntity(null, category, code, label, null, active);
        entity.setCreationDate(Instant.now());
        entity.setLastUpdateDate(Instant.now());
        entity.setLastUpdatedBy("test");
        return appConfigurationRepository.save(entity);
    }
}
