package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.houndjo.domain.models.auth.JwtToken;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.ports.in.OrganizationUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.TwoFactorChallengeResponse;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.LoginRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.TwoFactorLoginVerifyRequest;
import com.houndjo.infrastructure.adapter.out.persistence.entity.TwoFactorChallengeEntity;
import com.houndjo.infrastructure.adapter.out.persistence.repository.TwoFactorChallengeJpaRepository;
import com.houndjo.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class AuthenticationControllerTest extends IntegrationTest {
    private static final String AUTH_BASE_URL = "/api/auth";
    private static final String LOGIN_ROUTE = AUTH_BASE_URL + "/login";
    private static final String REFRESH_ROUTE = AUTH_BASE_URL + "/refresh";
    private static final String LOGOUT_ROUTE = AUTH_BASE_URL + "/logout";
    private static final String TWO_FACTOR_VERIFY_ROUTE = AUTH_BASE_URL + "/2fa/verify";

    @Autowired
    private TwoFactorChallengeJpaRepository twoFactorChallengeRepository;

    @Autowired
    private OrganizationUseCase organizationUseCase;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldAuthorizeUserWithoutRememberMe() throws Exception {
        createDefaultUser();
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);

        JwtToken jwtToken = post(
                LOGIN_ROUTE,
                login,
                JwtToken.class,
                status().isOk(),
                header().string(AUTHORIZATION, not(nullValue())),
                header().string(AUTHORIZATION, not(is(emptyString()))));

        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(jwtToken.refreshToken()).isNotBlank();
    }

    @Test
    void shouldAuthorizeUserWithRememberMe() throws Exception {
        createDefaultUser();
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, true);

        JwtToken jwtToken = post(
                LOGIN_ROUTE,
                login,
                JwtToken.class,
                status().isOk(),
                header().string(AUTHORIZATION, not(nullValue())),
                header().string(AUTHORIZATION, not(is(emptyString()))));

        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(jwtToken.refreshToken()).isNotBlank();
    }

    @Test
    void shouldNotAuthorizeInexistentUser() throws Exception {
        LoginRequest login = new LoginRequest("wrong-email@dev.com", "wrong password", false);

        post(LOGIN_ROUTE, login, status().isUnauthorized(), header().doesNotExist(AUTHORIZATION));
    }

    @Test
    void shouldRefreshAccessTokenWithValidRefreshToken() throws Exception {
        createDefaultUser();
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);

        JwtToken initialToken = post(LOGIN_ROUTE, login, JwtToken.class, status().isOk());

        JwtToken refreshedToken = postText(REFRESH_ROUTE, initialToken.refreshToken(), JwtToken.class, status().isOk());

        assertThat(refreshedToken).isNotNull();
        assertThat(refreshedToken.accessToken()).isNotBlank();
        assertThat(refreshedToken.refreshToken()).isNotBlank();
        assertThat(refreshedToken.accessToken()).isNotEqualTo(initialToken.accessToken());
    }

    @Test
    void shouldIncludeDefaultActiveOrganizationInLoginAndRefreshTokens() throws Exception {
        var user = createDefaultUser();
        Organization organization = organizationUseCase.registerSchool(
                Organization.create("Ecole Al Nour", "contact@al-nour.test", null, null, null, null), user.getId());
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);

        JwtToken initialToken = post(LOGIN_ROUTE, login, JwtToken.class, status().isOk());
        Long initialOrganizationId =
                jwtDecoder.decode(initialToken.accessToken()).getClaim("orgId");
        JwtToken refreshedToken = postText(REFRESH_ROUTE, initialToken.refreshToken(), JwtToken.class, status().isOk());
        Long refreshedOrganizationId =
                jwtDecoder.decode(refreshedToken.accessToken()).getClaim("orgId");

        assertThat(initialOrganizationId).isEqualTo(organization.getId());
        assertThat(refreshedOrganizationId).isEqualTo(organization.getId());
    }

    @Test
    void shouldNotRefreshAccessTokenWithExpiredRefreshToken() throws Exception {
        String expiredRefreshToken = "expired.refresh.token";

        postText(REFRESH_ROUTE, expiredRefreshToken, status().isUnauthorized());
    }

    @Test
    void shouldLogoutUserAndRevokeAccessToken() throws Exception {
        createDefaultUser();
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);
        JwtToken jwtToken = post(LOGIN_ROUTE, login, JwtToken.class, status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post(LOGOUT_ROUTE)
                        .header(AUTHORIZATION, "Bearer " + jwtToken.accessToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.post(LOGOUT_ROUTE)
                        .header(AUTHORIZATION, "Bearer " + jwtToken.accessToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLogoutWithoutAccessToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(LOGOUT_ROUTE)).andExpect(status().isUnauthorized());
    }

    // --- 2FA login tests ---

    @Test
    void shouldReturn202WithChallengeWhenTwoFactorEnabled() throws Exception {
        createUserWithTwoFactor(DEFAULT_USER_EMAIL, TwoFactorMethodType.EMAIL);
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);

        TwoFactorChallengeResponse response = post(
                LOGIN_ROUTE,
                login,
                TwoFactorChallengeResponse.class,
                status().isAccepted(),
                header().doesNotExist(AUTHORIZATION));

        assertThat(response).isNotNull();
        assertThat(response.challengeId()).isNotBlank();
        assertThat(response.type()).isEqualTo(TwoFactorMethodType.EMAIL);

        // Verify the challenge was persisted
        assertThat(twoFactorChallengeRepository.findById(response.challengeId()))
                .isPresent();
    }

    @Test
    void shouldCompleteLoginAfterTwoFactorVerification() throws Exception {
        createUserWithTwoFactor(DEFAULT_USER_EMAIL, TwoFactorMethodType.EMAIL);
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, true);

        // Step 1: Login returns 202 with challengeId
        TwoFactorChallengeResponse challengeResponse =
                post(LOGIN_ROUTE, login, TwoFactorChallengeResponse.class, status().isAccepted());

        assertThat(challengeResponse).isNotNull();

        // Step 2: Read the OTP code from the DB challenge
        TwoFactorChallengeEntity challenge = twoFactorChallengeRepository
                .findById(challengeResponse.challengeId())
                .orElseThrow(() -> new AssertionError("Challenge not found in DB"));

        // Step 3: Verify the challenge to complete login
        TwoFactorLoginVerifyRequest verifyRequest =
                new TwoFactorLoginVerifyRequest(challengeResponse.challengeId(), challenge.getCode());

        JwtToken jwtToken = post(
                TWO_FACTOR_VERIFY_ROUTE,
                verifyRequest,
                JwtToken.class,
                status().isOk(),
                header().string(AUTHORIZATION, not(nullValue())));

        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(jwtToken.refreshToken()).isNotBlank();

        // Challenge must be deleted after successful verification
        assertThat(twoFactorChallengeRepository.findById(challengeResponse.challengeId()))
                .isEmpty();
    }

    @Test
    void shouldFailVerifyWithWrongCode() throws Exception {
        createUserWithTwoFactor(DEFAULT_USER_EMAIL, TwoFactorMethodType.EMAIL);
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);

        TwoFactorChallengeResponse challengeResponse =
                post(LOGIN_ROUTE, login, TwoFactorChallengeResponse.class, status().isAccepted());

        TwoFactorLoginVerifyRequest verifyRequest = new TwoFactorLoginVerifyRequest(
                challengeResponse.challengeId(), "000000" // wrong code
                );

        post(TWO_FACTOR_VERIFY_ROUTE, verifyRequest, status().isBadRequest());
    }

    @Test
    void shouldFailVerifyWithInvalidChallengeId() throws Exception {
        TwoFactorLoginVerifyRequest verifyRequest =
                new TwoFactorLoginVerifyRequest("non-existent-challenge-id", "123456");

        post(TWO_FACTOR_VERIFY_ROUTE, verifyRequest, status().isBadRequest());
    }

    // --- Global 2FA enforcement tests ---

    @Test
    void shouldRejectLoginWhenGlobalTwoFactorRequiredAndUserHasNoTwoFactor() throws Exception {
        enableGlobalTwoFactorRequirement();
        createDefaultUser(); // user without 2FA
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);

        post(LOGIN_ROUTE, login, status().isForbidden(), header().doesNotExist(AUTHORIZATION));
    }

    @Test
    void shouldAllowLoginWhenGlobalTwoFactorRequiredAndUserHasTwoFactorEnabled() throws Exception {
        enableGlobalTwoFactorRequirement();
        createUserWithTwoFactor(DEFAULT_USER_EMAIL, TwoFactorMethodType.EMAIL);
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);

        // Should return 202 with challenge (2FA required — user has it configured)
        TwoFactorChallengeResponse response = post(
                LOGIN_ROUTE,
                login,
                TwoFactorChallengeResponse.class,
                status().isAccepted(),
                header().doesNotExist(AUTHORIZATION));

        assertThat(response).isNotNull();
        assertThat(response.challengeId()).isNotBlank();
    }

    @Test
    void shouldAllowNormalLoginWhenGlobalTwoFactorNotRequired() throws Exception {
        // No SecuritySettings configured — defaults to twoFactorRequired=false
        createDefaultUser();
        LoginRequest login = new LoginRequest(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, false);

        JwtToken jwtToken = post(LOGIN_ROUTE, login, JwtToken.class, status().isOk());

        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.accessToken()).isNotBlank();
    }
}
