package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.models.auth.JwtToken;
import com.houndjo.domain.ports.in.TwoFactorUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.TotpSetupResponse;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.TwoFactorDisableRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.TwoFactorLoginVerifyRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.TwoFactorSetupConfirmRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.TwoFactorSetupRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for two-factor authentication management and login challenge verification.
 */
@RestController
@Tag(name = "Two-Factor Authentication management")
@Validated
@RequestMapping(path = "/api", version = "1.0")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorUseCase twoFactorUseCase;

    /**
     * Verifies a 2FA login challenge and returns JWT tokens.
     * This endpoint is public (no JWT required) since the user is not yet authenticated.
     */
    @PostMapping("/auth/2fa/verify")
    public ResponseEntity<JwtToken> verifyLoginChallenge(@Valid @RequestBody TwoFactorLoginVerifyRequest request) {
        JwtToken token = twoFactorUseCase.verifyLoginChallenge(request.challengeId(), request.code());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.accessToken());

        return new ResponseEntity<>(token, headers, HttpStatus.OK);
    }

    /**
     * Initiates 2FA setup for the current authenticated user.
     * <ul>
     *   <li>Email/SMS: sends a verification code and returns 204 No Content.</li>
     *   <li>TOTP: returns 200 OK with {@code {secret, otpAuthUri}} for QR-code display.</li>
     * </ul>
     */
    @PostMapping("/accounts/me/2fa/setup")
    public ResponseEntity<?> init2FactorSetup(@Valid @RequestBody TwoFactorSetupRequest request) {
        return twoFactorUseCase
                .initSetup(request.type())
                .map(data -> ResponseEntity.ok((Object) new TotpSetupResponse(data.secret(), data.otpAuthUri())))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Confirms 2FA setup by verifying the code and enables 2FA on the account.
     */
    @PostMapping("/accounts/me/2fa/setup/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirm2FactorSetup(@Valid @RequestBody TwoFactorSetupConfirmRequest request) {
        twoFactorUseCase.confirmSetup(request.code());
    }

    /**
     * Disables 2FA for the current authenticated user.
     * Requires the current password as confirmation.
     */
    @DeleteMapping("/accounts/me/2fa")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable2Factor(@Valid @RequestBody TwoFactorDisableRequest request) {
        twoFactorUseCase.disable(request.currentPassword());
    }
}
