package com.houndjo.integration.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ValidationErrorResponseDTO;
import com.houndjo.integration.IntegrationTest;
import com.houndjo.integration.tenancy.TenantFixtureController.TenantFixtureRecord;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Proves the ADR-001 tenant isolation guard end-to-end: {@link com.houndjo.application.tenant.TenantContext}
 * resolves the organization from the {@code orgId} JWT claim, and a request with none is
 * rejected before any tenant-scoped data can leak.
 */
class TenantContextIsolationTest extends IntegrationTest {

    private static final String API = "/api/test/tenant-fixture";
    private static final long ORG_A = 1L;
    private static final long ORG_B = 2L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetFixtureTable() {
        jdbcTemplate.execute("DELETE FROM tenant_fixture_test");
    }

    @Test
    void shouldOnlyListRecordsBelongingToTheActiveOrganization() throws Exception {
        insertFixtureRecord(ORG_A, "Org A record");
        insertFixtureRecord(ORG_B, "Org B record");

        List<TenantFixtureRecord> result = getAsOrganization(ORG_A);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Org A record");
    }

    @Test
    void shouldIsolateEachOrganizationsData() throws Exception {
        Long orgARecordId = insertFixtureRecord(ORG_A, "Org A record");
        insertFixtureRecord(ORG_B, "Org B record 1");
        insertFixtureRecord(ORG_B, "Org B record 2");

        List<TenantFixtureRecord> orgAResult = getAsOrganization(ORG_A);
        List<TenantFixtureRecord> orgBResult = getAsOrganization(ORG_B);

        assertThat(orgAResult).extracting(TenantFixtureRecord::id).containsExactly(orgARecordId);
        assertThat(orgBResult).hasSize(2);
    }

    @Test
    void shouldRejectRequestWithNoActiveOrganizationWithBadRequest() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(API)
                        .with(jwt().jwt(j -> j.subject("no-org-user@test.com"))
                                .authorities(new SimpleGrantedAuthority("user:read:own")))
                        .header("Accept-Language", "en"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ValidationErrorResponseDTO error = objectMapper.readValue(response, ValidationErrorResponseDTO.class);

        assertThat(error.status()).isEqualTo(400);
        assertThat(error.errors()).containsEntry("message", "No active organization selected for this request.");
    }

    @Test
    void shouldRejectMalformedStringOrganizationClaimWithBadRequest() throws Exception {
        assertInvalidOrganizationClaim("not-an-organization-id");
    }

    @Test
    void shouldRejectNonPositiveOrganizationClaimsWithBadRequest() throws Exception {
        assertInvalidOrganizationClaim(0L);
        assertInvalidOrganizationClaim(-1L);
        assertInvalidOrganizationClaim("0");
        assertInvalidOrganizationClaim("-1");
    }

    private Long insertFixtureRecord(long organizationId, String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO tenant_fixture_test (organization_id, name) VALUES (?, ?) RETURNING id",
                Long.class,
                organizationId,
                name);
    }

    private List<TenantFixtureRecord> getAsOrganization(long organizationId) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(API)
                        .with(jwt().jwt(j -> j.subject("org-user@test.com").claim("orgId", organizationId))
                                .authorities(new SimpleGrantedAuthority("user:read:own"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {});
    }

    private void assertInvalidOrganizationClaim(Object organizationClaim) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(API)
                        .with(jwt().jwt(j ->
                                        j.subject("invalid-org-user@test.com").claim("orgId", organizationClaim))
                                .authorities(new SimpleGrantedAuthority("user:read:own")))
                        .header("Accept-Language", "en"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ValidationErrorResponseDTO error = objectMapper.readValue(response, ValidationErrorResponseDTO.class);
        assertThat(error.errors()).containsEntry("message", "No active organization selected for this request.");
    }
}
