package com.houndjo.integration.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.houndjo.integration.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

// Low limits so we can exercise the rate limiter in a test without thousands of requests.
// This @TestPropertySource creates a dedicated Spring context, isolated from the main
// integration-test context (which uses high limits so other tests are never rate-limited).
@TestPropertySource(
        properties = {
            "app.rate-limit.auth.limit-for-period=3",
            "app.rate-limit.auth.limit-refresh-period=60s",
            "app.rate-limit.api.limit-for-period=5",
            "app.rate-limit.api.limit-refresh-period=60s"
        })
class RateLimitFilterTest extends IntegrationTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String ACCOUNT_URL = "/api/accounts/me";

    // Non-existent credentials: login returns 401 (auth logic reached → rate limiter passed the request)
    private static final String NO_USER_LOGIN_BODY =
            "{\"email\":\"noone@nowhere.dev\",\"password\":\"Wr0ngP@ss!\",\"rememberMe\":false}";

    // -------------------------------------------------------------------------
    // Auth tier tests (limit = 3 per 60s in test config)
    // Using non-existent credentials avoids JWT-token DB conflicts on repeated logins.
    // 401 means the rate limiter let the request through; 429 means it blocked it.
    // -------------------------------------------------------------------------

    @Test
    void shouldAllowRequestsWithinAuthRateLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(NO_USER_LOGIN_BODY)
                            .with(req -> {
                                req.setRemoteAddr("10.1.1.1");
                                return req;
                            }))
                    .andExpect(status().isUnauthorized()); // 401 = allowed through, login failed
        }
    }

    @Test
    @WithMockUser(username = DEFAULT_USER_EMAIL)
    void shouldRejectRequestExceedingAuthRateLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(NO_USER_LOGIN_BODY)
                            .with(req -> {
                                req.setRemoteAddr("10.2.2.2");
                                return req;
                            }))
                    .andExpect(status().isUnauthorized()); // allowed
        }
        // 4th request must be blocked by the rate limiter
        mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(NO_USER_LOGIN_BODY)
                        .with(req -> {
                            req.setRemoteAddr("10.2.2.2");
                            return req;
                        }))
                .andExpect(status().isTooManyRequests());
    }

    // -------------------------------------------------------------------------
    // API tier – IP bucket tests (limit = 5 per 60s in test config)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = DEFAULT_USER_EMAIL, authorities = "user:read:own")
    void shouldAllowRequestsWithinApiRateLimit() throws Exception {
        createDefaultUser();
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_URL).with(req -> {
                        req.setRemoteAddr("10.3.3.3");
                        return req;
                    }))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(username = DEFAULT_USER_EMAIL, authorities = "user:read:own")
    void shouldRejectRequestExceedingApiRateLimit() throws Exception {
        createDefaultUser();
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_URL).with(req -> {
                        req.setRemoteAddr("10.4.4.4");
                        return req;
                    }))
                    .andExpect(status().isOk());
        }
        // 6th request must be blocked
        mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_URL).with(req -> {
                    req.setRemoteAddr("10.4.4.4");
                    return req;
                }))
                .andExpect(status().isTooManyRequests());
    }

    // -------------------------------------------------------------------------
    // API tier – user+device bucket tests
    // Using jwt() post-processor sets up JwtAuthenticationToken so the filter
    // checks both the IP bucket and the user+device bucket.
    // -------------------------------------------------------------------------

    @Test
    void shouldEnforcePerUserRateLimit() throws Exception {
        // Create a real user so GET /accounts/me returns 200
        createUser("rate-limit-user5@test.com");
        String userId = "rate-limit-user5@test.com";

        // Different IPs → fresh IP bucket per request; same user+device bucket
        for (int i = 1; i <= 5; i++) {
            final int idx = i;
            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_URL)
                            .with(jwt().jwt(j -> j.subject(userId))
                                    .authorities(new SimpleGrantedAuthority("user:read:own")))
                            .with(req -> {
                                req.setRemoteAddr("10.5.0." + idx);
                                return req;
                            }))
                    .andExpect(status().isOk());
        }
        // 6th: fresh IP but user+device bucket is exhausted → 429
        mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_URL)
                        .with(jwt().jwt(j -> j.subject(userId))
                                .authorities(new SimpleGrantedAuthority("user:read:own")))
                        .with(req -> {
                            req.setRemoteAddr("10.5.0.6");
                            return req;
                        }))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldAllowIndependentLimitsPerDevice() throws Exception {
        // Create a real user so GET /accounts/me returns 200
        createUser("rate-limit-user6@test.com");
        String userId = "rate-limit-user6@test.com";

        // 3 requests with device-A (uses 3 of 5 permits for device-A)
        for (int i = 1; i <= 3; i++) {
            final int idx = i;
            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_URL)
                            .with(jwt().jwt(j -> j.subject(userId))
                                    .authorities(new SimpleGrantedAuthority("user:read:own")))
                            .header("X-Device-ID", "device-A")
                            .with(req -> {
                                req.setRemoteAddr("10.6.0." + idx);
                                return req;
                            }))
                    .andExpect(status().isOk());
        }
        // 3 requests with device-B (uses 3 of 5 permits for device-B, independent)
        for (int i = 4; i <= 6; i++) {
            final int idx = i;
            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_URL)
                            .with(jwt().jwt(j -> j.subject(userId))
                                    .authorities(new SimpleGrantedAuthority("user:read:own")))
                            .header("X-Device-ID", "device-B")
                            .with(req -> {
                                req.setRemoteAddr("10.6.0." + idx);
                                return req;
                            }))
                    .andExpect(status().isOk());
        }
    }

    // -------------------------------------------------------------------------
    // Retry-After header
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = DEFAULT_USER_EMAIL)
    void shouldReturn429WithRetryAfterHeader() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(NO_USER_LOGIN_BODY)
                            .with(req -> {
                                req.setRemoteAddr("10.8.8.8");
                                return req;
                            }))
                    .andExpect(status().isUnauthorized()); // allowed
        }
        mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(NO_USER_LOGIN_BODY)
                        .with(req -> {
                            req.setRemoteAddr("10.8.8.8");
                            return req;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    // -------------------------------------------------------------------------
    // Disabled mode (separate Spring context via @TestPropertySource)
    // -------------------------------------------------------------------------

    @Nested
    @TestPropertySource(properties = "app.rate-limit.enabled=false")
    class WhenRateLimitDisabled extends IntegrationTest {

        @Test
        void shouldAllowRequestsWhenRateLimitDisabled() throws Exception {
            // auth limit is 3, send 4 — all should pass because rate limiting is off
            for (int i = 0; i < 4; i++) {
                mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                                .contentType(APPLICATION_JSON_VALUE)
                                .content(NO_USER_LOGIN_BODY)
                                .with(req -> {
                                    req.setRemoteAddr("10.9.9.9");
                                    return req;
                                }))
                        .andExpect(status().isUnauthorized()); // 401 = passed rate limiter, login failed
            }
        }
    }
}
