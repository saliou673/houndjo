package com.houndjo.infrastructure.adapter.out.twofactor;

import com.houndjo.config.ApplicationProperties;
import com.houndjo.domain.models.auth.TotpSetupData;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.out.TwoFactorProviderPort;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

/**
 * TOTP two-factor authentication provider (Google Authenticator compatible).
 * Implements RFC 6238 (TOTP) using HMAC-SHA1, 6 digits, 30-second period.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TotpTwoFactorProviderAdapter implements TwoFactorProviderPort {

    private static final int SECRET_BYTES = 20; // 160-bit HMAC-SHA1 key
    private static final int TOTP_DIGITS = 6;
    private static final int TOTP_PERIOD = 30; // seconds
    private static final int TOTP_WINDOW = 1; // steps allowed before/after current
    private static final Base32 BASE32 = new Base32();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ApplicationProperties applicationProperties;

    @Override
    public TwoFactorMethodType getType() {
        return TwoFactorMethodType.TOTP;
    }

    /**
     * Generates a new TOTP shared secret and returns it as a Base32-encoded string.
     * Nothing is sent to the user — the secret is returned via {@link #buildSetupData}.
     */
    @Override
    public String generateAndSendCode(User user) {
        byte[] secretBytes = new byte[SECRET_BYTES];
        SECURE_RANDOM.nextBytes(secretBytes);
        return BASE32.encodeToString(secretBytes);
    }

    /**
     * Verifies the TOTP code provided by the user.
     * <ul>
     *   <li>During <b>login</b>: uses {@code user.getTotpSecret()} (already set on the user).</li>
     *   <li>During <b>setup confirmation</b>: {@code user.getTotpSecret()} is still null — falls back
     *       to {@code storedCode}, which holds the pending secret from the challenge.</li>
     * </ul>
     * Accepts codes within a ±1 step window to handle clock drift.
     */
    @Override
    public boolean verify(User user, String storedCode, String providedCode) {
        String secret = user.getTotpSecret() != null ? user.getTotpSecret() : storedCode;
        if (secret == null || secret.isEmpty() || providedCode == null) {
            return false;
        }
        try {
            byte[] key = BASE32.decode(secret);
            long currentStep = Instant.now().getEpochSecond() / TOTP_PERIOD;
            for (int i = -TOTP_WINDOW; i <= TOTP_WINDOW; i++) {
                String expected = computeTotp(key, currentStep + i);
                if (expected.equals(providedCode)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn(
                    "TOTP verification failed for user: {}",
                    user.getUserCredentials().getEmail(),
                    e);
            return false;
        }
    }

    /**
     * Builds the TOTP setup data: the raw secret and an {@code otpauth://} URI
     * that the user's authenticator app can import by scanning a QR code.
     */
    @Override
    public Optional<TotpSetupData> buildSetupData(User user, String pendingSecret) {
        String issuer = applicationProperties.getTwoFactor().totpIssuer();
        String email = user.getUserCredentials().getEmail();
        String label = encode(issuer) + ":" + encode(email);
        String otpAuthUri = "otpauth://totp/" + label
                + "?secret=" + pendingSecret
                + "&issuer=" + encode(issuer)
                + "&algorithm=SHA1"
                + "&digits=" + TOTP_DIGITS
                + "&period=" + TOTP_PERIOD;
        return Optional.of(new TotpSetupData(pendingSecret, otpAuthUri));
    }

    // -------------------------------------------------------------------------
    // TOTP computation — RFC 6238 / RFC 4226
    // -------------------------------------------------------------------------

    private static String computeTotp(byte[] key, long timeStep) throws Exception {
        byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
        Mac hmac = Mac.getInstance("HmacSHA1");
        hmac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = hmac.doFinal(data);

        // Dynamic truncation (RFC 4226 §5.4)
        int offset = hash[hash.length - 1] & 0x0F;
        int code = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        return String.format("%0" + TOTP_DIGITS + "d", code % (int) Math.pow(10, TOTP_DIGITS));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
