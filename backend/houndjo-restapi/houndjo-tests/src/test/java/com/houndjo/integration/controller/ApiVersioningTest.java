package com.houndjo.integration.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.houndjo.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class ApiVersioningTest extends IntegrationTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String LOGIN_BODY =
            "{\"email\":\"versioning-test@test.dev\",\"password\":\"Wr0ng!\",\"rememberMe\":false}";

    @Test
    void shouldAcceptRequestWithSupportedVersion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(LOGIN_BODY)
                        .header("X-API-Version", "1.0"))
                .andExpect(status().isUnauthorized()); // 401 = routed to controller, version accepted
    }

    @Test
    void shouldAcceptRequestWithoutVersionHeaderUsingDefault() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(LOGIN_BODY))
                .andExpect(status().isUnauthorized()); // 401 = routed to controller, default version applied
    }

    @Test
    void shouldRejectRequestWithUnsupportedVersion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(LOGIN_BODY)
                        .header("X-API-Version", "2.0"))
                .andExpect(status().isBadRequest()); // 400 = rejected by versioning layer before routing
    }
}
