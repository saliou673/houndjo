package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.houndjo.domain.models.auth.JwtToken;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ValidationErrorResponseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.LoginRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.PasswordChangeRequest;
import com.houndjo.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.houndjo.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Verifies that domain exception messages are resolved to the correct locale by
 * {@code GlobalExceptionHandler}: anonymous requests fall back to the {@code Accept-Language}
 * header, while authenticated requests use the connected user's stored {@code languageKey},
 * which takes precedence over whatever {@code Accept-Language} the client sends (see
 * {@code LocaleConfiguration}).
 */
class ExceptionLocalizationTest extends IntegrationTest {

    private static final String LOGIN_ROUTE = "/api/auth/login";
    private static final String CHANGE_PASSWORD_ROUTE = "/api/accounts/me/password";

    @Test
    void shouldLocalizeAnonymousExceptionUsingAcceptLanguageHeader_French() throws Exception {
        createUser("known-user@dev.com");
        LoginRequest login = new LoginRequest("known-user@dev.com", "totally-wrong-password", false);

        ValidationErrorResponseDTO response = performAndReadError(
                MockMvcRequestBuilders.post(LOGIN_ROUTE)
                        .header(ACCEPT_LANGUAGE, "fr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)),
                status().isUnauthorized());

        assertThat(response.errors().get("message")).isEqualTo("E-mail ou mot de passe incorrect.");
    }

    @Test
    void shouldLocalizeAnonymousExceptionUsingAcceptLanguageHeader_English() throws Exception {
        createUser("known-user@dev.com");
        LoginRequest login = new LoginRequest("known-user@dev.com", "totally-wrong-password", false);

        ValidationErrorResponseDTO response = performAndReadError(
                MockMvcRequestBuilders.post(LOGIN_ROUTE)
                        .header(ACCEPT_LANGUAGE, "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)),
                status().isUnauthorized());

        assertThat(response.errors().get("message")).isEqualTo("Incorrect email or password.");
    }

    @Test
    void shouldUseConnectedUsersLanguageKeyOverAcceptLanguageHeader_French() throws Exception {
        UserEntity user = createUser("french-user@dev.com");
        user.getUserInfo().setLanguageKey("fr");
        userRepository.save(user);
        String accessToken = login("french-user@dev.com");

        PasswordChangeRequest request = new PasswordChangeRequest("wrong-current-password", "newPassword123!");

        // Client sends English, but the connected user's languageKey (fr) must win.
        ValidationErrorResponseDTO response = performAndReadError(
                MockMvcRequestBuilders.patch(CHANGE_PASSWORD_ROUTE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .header(ACCEPT_LANGUAGE, "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                status().isBadRequest());

        assertThat(response.errors().get("message")).isEqualTo("Le mot de passe actuel est incorrect.");
    }

    @Test
    void shouldUseConnectedUsersLanguageKeyOverAcceptLanguageHeader_English() throws Exception {
        UserEntity user = createUser("english-user@dev.com");
        user.getUserInfo().setLanguageKey("en");
        userRepository.save(user);
        String accessToken = login("english-user@dev.com");

        PasswordChangeRequest request = new PasswordChangeRequest("wrong-current-password", "newPassword123!");

        // Client sends French, but the connected user's languageKey (en) must win.
        ValidationErrorResponseDTO response = performAndReadError(
                MockMvcRequestBuilders.patch(CHANGE_PASSWORD_ROUTE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .header(ACCEPT_LANGUAGE, "fr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                status().isBadRequest());

        assertThat(response.errors().get("message")).isEqualTo("The current password is incorrect.");
    }

    private String login(String email) throws Exception {
        LoginRequest login = new LoginRequest(email, DEFAULT_USER_PASSWORD, false);
        JwtToken jwtToken = post(LOGIN_ROUTE, login, JwtToken.class, status().isOk());
        return jwtToken.accessToken();
    }

    private ValidationErrorResponseDTO performAndReadError(
            MockHttpServletRequestBuilder builder, org.springframework.test.web.servlet.ResultMatcher statusMatcher)
            throws Exception {
        String json = mockMvc.perform(builder)
                .andExpect(statusMatcher)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(json, ValidationErrorResponseDTO.class);
    }
}
