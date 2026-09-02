package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.models.auth.JwtToken;
import com.houndjo.domain.models.auth.LoginResult;
import com.houndjo.domain.ports.in.AuthenticationUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.TwoFactorChallengeResponse;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.LoginRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints to authenticate users.
 */
@Slf4j
@RestController
@Tag(name = "Authentication Management")
@RequestMapping(path = "/api/auth", version = "1.0")
@Validated
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationUseCase authenticationUseCase;

    /**
     * Authenticate a user and return a JWT token.
     * If 2FA is enabled on the account, returns HTTP 202 with a challenge ID instead.
     *
     * @param loginForm The user credentials
     * @return 200 OK with JWT, or 202 Accepted with TwoFactorChallengeResponse
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequest loginForm) {
        LoginResult result =
                authenticationUseCase.login(loginForm.email(), loginForm.password(), loginForm.rememberMe());

        return switch (result) {
            case LoginResult.Complete complete -> {
                JwtToken token = complete.token();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token.accessToken());
                yield new ResponseEntity<>(token, headers, HttpStatus.OK);
            }
            case LoginResult.TwoFactorRequired twoFactor ->
                ResponseEntity.accepted()
                        .body(new TwoFactorChallengeResponse(twoFactor.challengeId(), twoFactor.type()));
        };
    }

    /**
     * Refresh the access token using a valid refresh token.
     *
     * @param refreshToken The refresh token
     */
    @PostMapping(value = "/refresh", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<JwtToken> refreshToken(@RequestBody String refreshToken) {
        JwtToken newTokens = authenticationUseCase.refreshToken(refreshToken);
        return ResponseEntity.ok(newTokens);
    }

    /**
     * Log out the authenticated user.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt) {
        authenticationUseCase.logout(jwt.getTokenValue());
    }
}
