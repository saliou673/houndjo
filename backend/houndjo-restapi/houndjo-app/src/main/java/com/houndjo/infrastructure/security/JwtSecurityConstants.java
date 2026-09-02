package com.houndjo.infrastructure.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
/** Shared JWT security constants used by the token generation and validation infrastructure. */
public final class JwtSecurityConstants {
    /**
     * HMAC-SHA256 algorithm used to sign tokens.
     */
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;
    /**
     * Claim name under which the authorities string is stored in the JWT payload.
     */
    public static final String AUTHORITIES_KEY = "auth";
}
