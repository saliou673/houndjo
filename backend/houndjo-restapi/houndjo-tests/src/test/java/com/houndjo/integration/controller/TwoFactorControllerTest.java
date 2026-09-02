package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.houndjo.domain.models.auth.JwtToken;
import com.houndjo.domain.models.auth.TwoFactorChallengePurpose;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.TotpSetupResponse;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.TwoFactorChallengeResponse;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.*;
import com.houndjo.infrastructure.adapter.out.persistence.entity.TwoFactorChallengeEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.houndjo.infrastructure.adapter.out.persistence.repository.TwoFactorChallengeJpaRepository;
import com.houndjo.integration.IntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
class TwoFactorControllerTest extends IntegrationTest {

    private static final String API_2FA_VERIFY = "/api/auth/2fa/verify";
    private static final String API_2FA_SETUP = "/api/accounts/me/2fa/setup";
    private static final String API_2FA_SETUP_CONFIRM = "/api/accounts/me/2fa/setup/confirm";
    private static final String API_2FA_DISABLE = "/api/accounts/me/2fa";
    private static final String LOGIN_ROUTE = "/api/auth/login";

    @Autowired
    private TwoFactorChallengeJpaRepository twoFactorChallengeRepository;

    // =====================================================================
    // 2FA Setup — init
    // =====================================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldInit2FactorSetupSuccessfully() throws Exception {
        createUser(TEST_EMAIL);

        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isNoContent());

        // A SETUP challenge should be created in the DB
        UserEntity user = userRepository
                .findOneByUserCredentialsEmailIgnoreCase(TEST_EMAIL)
                .orElseThrow();
        assertThat(twoFactorChallengeRepository.findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP))
                .isPresent();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldFailInit2FactorSetupWhenTwoFactorAlreadyEnabled() throws Exception {
        createUserWithTwoFactor(TEST_EMAIL, TwoFactorMethodType.EMAIL);

        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isConflict());

        // No new challenge should be created
        UserEntity user = userRepository
                .findOneByUserCredentialsEmailIgnoreCase(TEST_EMAIL)
                .orElseThrow();
        assertThat(twoFactorChallengeRepository.findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP))
                .isEmpty();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldReplaceExistingSetupChallengeOnNewInit() throws Exception {
        createUser(TEST_EMAIL);

        // First init
        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isNoContent());
        // Second init — should replace the first challenge
        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isNoContent());

        UserEntity user = userRepository
                .findOneByUserCredentialsEmailIgnoreCase(TEST_EMAIL)
                .orElseThrow();
        // Still only one SETUP challenge
        assertThat(twoFactorChallengeRepository.findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP))
                .isPresent();
        assertThat(twoFactorChallengeRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectSetupInitWithoutAuthentication() throws Exception {
        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isUnauthorized());
    }

    // =====================================================================
    // 2FA Setup — confirm
    // =====================================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldConfirm2FactorSetupSuccessfully() throws Exception {
        UserEntity user = createUser(TEST_EMAIL);

        // Init setup to create a challenge
        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isNoContent());

        // Read the code from the DB
        TwoFactorChallengeEntity challenge = twoFactorChallengeRepository
                .findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP)
                .orElseThrow(() -> new AssertionError("Setup challenge not found"));
        String code = challenge.getCode();

        // Confirm setup
        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest(code), status().isNoContent());

        // 2FA should now be enabled on the user
        UserEntity updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.isTwoFactorEnabled()).isTrue();
        assertThat(updatedUser.getTwoFactorMethod()).isEqualTo(TwoFactorMethodType.EMAIL);

        // Challenge must be deleted after successful confirmation
        assertThat(twoFactorChallengeRepository.findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP))
                .isEmpty();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldFailConfirm2FactorSetupWithWrongCode() throws Exception {
        UserEntity user = createUser(TEST_EMAIL);
        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isNoContent());

        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest("000000"), status().isBadRequest());

        // 2FA should NOT be enabled
        UserEntity unchanged = userRepository.findById(user.getId()).orElseThrow();
        assertThat(unchanged.isTwoFactorEnabled()).isFalse();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldFailConfirm2FactorSetupWithExpiredChallenge() throws Exception {
        UserEntity user = createUser(TEST_EMAIL);

        // Create an expired SETUP challenge directly in the DB
        TwoFactorChallengeEntity expiredChallenge = new TwoFactorChallengeEntity(
                UUID.randomUUID().toString(),
                userRepository.findById(user.getId()).orElseThrow(),
                "123456",
                TwoFactorMethodType.EMAIL,
                TwoFactorChallengePurpose.SETUP,
                false,
                Instant.now().minus(10, ChronoUnit.MINUTES), // already expired
                Instant.now().minus(15, ChronoUnit.MINUTES));
        twoFactorChallengeRepository.save(expiredChallenge);

        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest("123456"), status().isBadRequest());

        UserEntity unchanged = userRepository.findById(user.getId()).orElseThrow();
        assertThat(unchanged.isTwoFactorEnabled()).isFalse();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldFailConfirm2FactorSetupWithNoChallenge() throws Exception {
        createUser(TEST_EMAIL);

        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest("123456"), status().isBadRequest());
    }

    @Test
    void shouldRejectSetupConfirmWithoutAuthentication() throws Exception {
        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest("123456"), status().isUnauthorized());
    }

    // =====================================================================
    // 2FA Disable
    // =====================================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldDisable2FactorTwoFactorSuccessfully() throws Exception {
        UserEntity user = createUserWithTwoFactor(TEST_EMAIL, TwoFactorMethodType.EMAIL);

        delete(API_2FA_DISABLE, new TwoFactorDisableRequest(DEFAULT_USER_PASSWORD), status().isNoContent());

        UserEntity updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.isTwoFactorEnabled()).isFalse();
        assertThat(updatedUser.getTwoFactorMethod()).isNull();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldFailDisable2FactorTwoFactorWithWrongPassword() throws Exception {
        UserEntity user = createUserWithTwoFactor(TEST_EMAIL, TwoFactorMethodType.EMAIL);

        delete(API_2FA_DISABLE, new TwoFactorDisableRequest("WrongPassword123!"), status().isBadRequest());

        UserEntity unchanged = userRepository.findById(user.getId()).orElseThrow();
        assertThat(unchanged.isTwoFactorEnabled()).isTrue();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldFailDisable2FactorTwoFactorWhenNotEnabled() throws Exception {
        createUser(TEST_EMAIL);

        delete(API_2FA_DISABLE, new TwoFactorDisableRequest(DEFAULT_USER_PASSWORD), status().isBadRequest());
    }

    @Test
    void shouldRejectDisable2FactorTwoFactorWithoutAuthentication() throws Exception {
        delete(API_2FA_DISABLE, new TwoFactorDisableRequest(DEFAULT_USER_PASSWORD), status().isUnauthorized());
    }

    // =====================================================================
    // 2FA Login challenge verification
    // =====================================================================

    @Test
    void shouldVerifyLoginChallengeSuccessfully() throws Exception {
        UserEntity user = createUserWithTwoFactor(TEST_EMAIL, TwoFactorMethodType.EMAIL);

        // Create a LOGIN challenge directly in the DB
        String challengeId = UUID.randomUUID().toString();
        String code = "654321";
        TwoFactorChallengeEntity challenge = new TwoFactorChallengeEntity(
                challengeId,
                userRepository.findById(user.getId()).orElseThrow(),
                code,
                TwoFactorMethodType.EMAIL,
                TwoFactorChallengePurpose.LOGIN,
                false,
                Instant.now().plus(5, ChronoUnit.MINUTES),
                Instant.now());
        twoFactorChallengeRepository.save(challenge);

        JwtToken jwtToken = post(
                API_2FA_VERIFY, new TwoFactorLoginVerifyRequest(challengeId, code), JwtToken.class, status().isOk());

        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(jwtToken.refreshToken()).isNotBlank();

        // Challenge must be consumed
        assertThat(twoFactorChallengeRepository.findById(challengeId)).isEmpty();
    }

    @Test
    void shouldFailVerifyLoginChallengeWithWrongCode() throws Exception {
        UserEntity user = createUserWithTwoFactor(TEST_EMAIL, TwoFactorMethodType.EMAIL);

        String challengeId = UUID.randomUUID().toString();
        TwoFactorChallengeEntity challenge = new TwoFactorChallengeEntity(
                challengeId,
                userRepository.findById(user.getId()).orElseThrow(),
                "111111",
                TwoFactorMethodType.EMAIL,
                TwoFactorChallengePurpose.LOGIN,
                false,
                Instant.now().plus(5, ChronoUnit.MINUTES),
                Instant.now());
        twoFactorChallengeRepository.save(challenge);

        post(API_2FA_VERIFY, new TwoFactorLoginVerifyRequest(challengeId, "999999"), status().isBadRequest());

        // Challenge must NOT be consumed on wrong code
        assertThat(twoFactorChallengeRepository.findById(challengeId)).isPresent();
    }

    @Test
    void shouldFailVerifyLoginChallengeWithExpiredChallenge() throws Exception {
        UserEntity user = createUserWithTwoFactor(TEST_EMAIL, TwoFactorMethodType.EMAIL);

        String challengeId = UUID.randomUUID().toString();
        TwoFactorChallengeEntity expiredChallenge = new TwoFactorChallengeEntity(
                challengeId,
                userRepository.findById(user.getId()).orElseThrow(),
                "123456",
                TwoFactorMethodType.EMAIL,
                TwoFactorChallengePurpose.LOGIN,
                false,
                Instant.now().minus(10, ChronoUnit.MINUTES), // expired
                Instant.now().minus(15, ChronoUnit.MINUTES));
        twoFactorChallengeRepository.save(expiredChallenge);

        post(API_2FA_VERIFY, new TwoFactorLoginVerifyRequest(challengeId, "123456"), status().isBadRequest());
    }

    @Test
    void shouldFailVerifyLoginChallengeWithNonExistentId() throws Exception {
        post(API_2FA_VERIFY, new TwoFactorLoginVerifyRequest("non-existent-id", "123456"), status().isBadRequest());
    }

    @Test
    void shouldRejectVerifyWithSetupChallengeInsteadOfLoginChallenge() throws Exception {
        UserEntity user = createUserWithTwoFactor(TEST_EMAIL, TwoFactorMethodType.EMAIL);

        // Create a SETUP challenge (wrong purpose for /auth/2fa/verify)
        String challengeId = UUID.randomUUID().toString();
        TwoFactorChallengeEntity setupChallenge = new TwoFactorChallengeEntity(
                challengeId,
                userRepository.findById(user.getId()).orElseThrow(),
                "123456",
                TwoFactorMethodType.EMAIL,
                TwoFactorChallengePurpose.SETUP, // wrong purpose
                false,
                Instant.now().plus(5, ChronoUnit.MINUTES),
                Instant.now());
        twoFactorChallengeRepository.save(setupChallenge);

        post(API_2FA_VERIFY, new TwoFactorLoginVerifyRequest(challengeId, "123456"), status().isBadRequest());
    }

    // =====================================================================
    // End-to-end flows
    // =====================================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldPerformCompleteSetupFlow() throws Exception {
        UserEntity user = createUser(TEST_EMAIL);

        // 1. Init setup
        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isNoContent());

        // 2. Confirm setup with the generated code
        TwoFactorChallengeEntity challenge = twoFactorChallengeRepository
                .findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP)
                .orElseThrow(() -> new AssertionError("Setup challenge not found"));

        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest(challenge.getCode()), status().isNoContent());

        // 3. Verify 2FA is enabled
        UserEntity updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.isTwoFactorEnabled()).isTrue();
        assertThat(updatedUser.getTwoFactorMethod()).isEqualTo(TwoFactorMethodType.EMAIL);
        assertThat(twoFactorChallengeRepository.count()).isZero();
    }

    @Test
    void shouldPerformCompleteLoginWithTwoFactorFlow() throws Exception {
        // 1. Create user with 2FA enabled
        createUserWithTwoFactor(TEST_EMAIL, TwoFactorMethodType.EMAIL);

        // 2. Login — receives a 2FA challenge
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, DEFAULT_USER_PASSWORD, false);
        TwoFactorChallengeResponse challengeResponse =
                post(LOGIN_ROUTE, loginRequest, TwoFactorChallengeResponse.class, status().isAccepted());

        assertThat(challengeResponse.challengeId()).isNotBlank();
        assertThat(challengeResponse.type()).isEqualTo(TwoFactorMethodType.EMAIL);

        // 3. Read code from DB
        TwoFactorChallengeEntity challenge = twoFactorChallengeRepository
                .findById(challengeResponse.challengeId())
                .orElseThrow(() -> new AssertionError("Login challenge not found"));

        // 4. Verify challenge to obtain JWT
        JwtToken jwtToken = post(
                API_2FA_VERIFY,
                new TwoFactorLoginVerifyRequest(challenge.getId(), challenge.getCode()),
                JwtToken.class,
                status().isOk());

        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(jwtToken.refreshToken()).isNotBlank();

        // 5. Challenge consumed
        assertThat(twoFactorChallengeRepository.findById(challenge.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldPerformCompleteSetupAndDisable2FactorFlow() throws Exception {
        UserEntity user = createUser(TEST_EMAIL);

        // 1. Enable 2FA
        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.EMAIL), status().isNoContent());
        TwoFactorChallengeEntity challenge = twoFactorChallengeRepository
                .findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP)
                .orElseThrow();
        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest(challenge.getCode()), status().isNoContent());

        assertThat(userRepository.findById(user.getId()).orElseThrow().isTwoFactorEnabled())
                .isTrue();

        // 2. Disable 2FA
        delete(API_2FA_DISABLE, new TwoFactorDisableRequest(DEFAULT_USER_PASSWORD), status().isNoContent());

        UserEntity disabledUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(disabledUser.isTwoFactorEnabled()).isFalse();
        assertThat(disabledUser.getTwoFactorMethod()).isNull();
    }

    // =====================================================================
    // TOTP-specific tests
    // =====================================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldReturnTotpSetupDataOnInit2FactorSetup() throws Exception {
        createUser(TEST_EMAIL);

        TotpSetupResponse response = post(
                API_2FA_SETUP,
                new TwoFactorSetupRequest(TwoFactorMethodType.TOTP),
                TotpSetupResponse.class,
                status().isOk());

        assertThat(response).isNotNull();
        assertThat(response.secret()).isNotBlank();
        assertThat(response.otpAuthUri()).startsWith("otpauth://totp/");
        assertThat(response.otpAuthUri()).contains("secret=");
        assertThat(response.otpAuthUri()).contains("issuer=");

        // A pending SETUP challenge should exist in the DB holding the secret
        UserEntity user = userRepository
                .findOneByUserCredentialsEmailIgnoreCase(TEST_EMAIL)
                .orElseThrow();
        assertThat(twoFactorChallengeRepository.findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP))
                .isPresent()
                .hasValueSatisfying(challenge -> assertThat(challenge.getCode()).isEqualTo(response.secret()));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldEnableTotpAfterConfirm2FactorSetup() throws Exception {
        UserEntity user = createUser(TEST_EMAIL);

        // Init TOTP setup — returns secret + QR URI
        TotpSetupResponse setupResponse = post(
                API_2FA_SETUP,
                new TwoFactorSetupRequest(TwoFactorMethodType.TOTP),
                TotpSetupResponse.class,
                status().isOk());

        // Compute the correct TOTP code using the returned secret
        String validCode = computeTotpCode(setupResponse.secret());

        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest(validCode), status().isNoContent());

        UserEntity updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.isTwoFactorEnabled()).isTrue();
        assertThat(updatedUser.getTwoFactorMethod()).isEqualTo(TwoFactorMethodType.TOTP);
        assertThat(updatedUser.getTotpSecret()).isEqualTo(setupResponse.secret());

        // Challenge consumed
        assertThat(twoFactorChallengeRepository.findByUserIdAndPurpose(user.getId(), TwoFactorChallengePurpose.SETUP))
                .isEmpty();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void shouldRejectTotpConfirmWithWrongCode() throws Exception {
        UserEntity user = createUser(TEST_EMAIL);

        post(API_2FA_SETUP, new TwoFactorSetupRequest(TwoFactorMethodType.TOTP), status().isOk());

        post(API_2FA_SETUP_CONFIRM, new TwoFactorSetupConfirmRequest("000000"), status().isBadRequest());

        assertThat(userRepository.findById(user.getId()).orElseThrow().isTwoFactorEnabled())
                .isFalse();
    }

    @Test
    void shouldVerifyTotpLoginChallengeSuccessfully() throws Exception {
        // Seed a valid TOTP secret on the user
        String totpSecret = generateTotpSecret();
        UserEntity user = createUserWithTotp(TEST_EMAIL, totpSecret);

        // Create a LOGIN challenge (code is empty — TOTP uses user.totpSecret)
        String challengeId = UUID.randomUUID().toString();
        TwoFactorChallengeEntity challenge = new TwoFactorChallengeEntity(
                challengeId,
                userRepository.findById(user.getId()).orElseThrow(),
                "", // empty: TOTP uses the user's stored secret
                TwoFactorMethodType.TOTP,
                TwoFactorChallengePurpose.LOGIN,
                false,
                Instant.now().plus(5, ChronoUnit.MINUTES),
                Instant.now());
        twoFactorChallengeRepository.save(challenge);

        String validCode = computeTotpCode(totpSecret);

        JwtToken jwtToken = post(
                API_2FA_VERIFY,
                new TwoFactorLoginVerifyRequest(challengeId, validCode),
                JwtToken.class,
                status().isOk());

        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(twoFactorChallengeRepository.findById(challengeId)).isEmpty();
    }

    @Test
    void shouldRejectTotpLoginChallengeWithWrongCode() throws Exception {
        String totpSecret = generateTotpSecret();
        UserEntity user = createUserWithTotp(TEST_EMAIL, totpSecret);

        String challengeId = UUID.randomUUID().toString();
        twoFactorChallengeRepository.save(new TwoFactorChallengeEntity(
                challengeId,
                userRepository.findById(user.getId()).orElseThrow(),
                "",
                TwoFactorMethodType.TOTP,
                TwoFactorChallengePurpose.LOGIN,
                false,
                Instant.now().plus(5, ChronoUnit.MINUTES),
                Instant.now()));

        post(API_2FA_VERIFY, new TwoFactorLoginVerifyRequest(challengeId, "000000"), status().isBadRequest());

        // Challenge is preserved on failure
        assertThat(twoFactorChallengeRepository.findById(challengeId)).isPresent();
    }

    @Test
    void shouldPerformCompleteLoginWithTotpFlow() throws Exception {
        // Build a known TOTP secret, create a user with it enabled
        String totpSecret = generateTotpSecret();
        createUserWithTotp(TEST_EMAIL, totpSecret);

        // Login — should return 202 with a TOTP challenge
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, DEFAULT_USER_PASSWORD, false);
        TwoFactorChallengeResponse challengeResponse =
                post(LOGIN_ROUTE, loginRequest, TwoFactorChallengeResponse.class, status().isAccepted());

        assertThat(challengeResponse.challengeId()).isNotBlank();
        assertThat(challengeResponse.type()).isEqualTo(TwoFactorMethodType.TOTP);

        // Verify the challenge with a valid TOTP code
        String validCode = computeTotpCode(totpSecret);
        JwtToken jwtToken = post(
                API_2FA_VERIFY,
                new TwoFactorLoginVerifyRequest(challengeResponse.challengeId(), validCode),
                JwtToken.class,
                status().isOk());

        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(twoFactorChallengeRepository.findById(challengeResponse.challengeId()))
                .isEmpty();
    }

    // -------------------------------------------------------------------------
    // TOTP test helpers
    // -------------------------------------------------------------------------

    /**
     * Generates a random Base32 TOTP secret (same as the adapter).
     */
    private static String generateTotpSecret() {
        byte[] secretBytes = new byte[20];
        new java.security.SecureRandom().nextBytes(secretBytes);
        return new org.apache.commons.codec.binary.Base32().encodeToString(secretBytes);
    }

    /**
     * Computes the current TOTP code for the given Base32 secret (RFC 6238, HMAC-SHA1, 6 digits, 30s).
     */
    private static String computeTotpCode(String base32Secret) throws Exception {
        byte[] key = new org.apache.commons.codec.binary.Base32().decode(base32Secret);
        long timeStep = Instant.now().getEpochSecond() / 30;
        byte[] data = java.nio.ByteBuffer.allocate(8).putLong(timeStep).array();
        javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA1");
        hmac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = hmac.doFinal(data);
        int offset = hash[hash.length - 1] & 0x0F;
        int code = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
        return String.format("%06d", code % 1_000_000);
    }

    private static final String TEST_EMAIL = "test@example.com";
}
