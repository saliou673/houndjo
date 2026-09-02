package com.houndjo.config;

import com.houndjo.domain.ports.out.persistenceport.AuthTokenPersistencePort;
import com.houndjo.infrastructure.metrics.SecurityMetersService;
import com.houndjo.infrastructure.security.JwtSecurityConstants;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Spring configuration providing JWT encoder, decoder, and authentication converter beans.
 */
@Slf4j
@Configuration
public class SecurityJwtConfiguration {

    @Value("${app.security.authentication.jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public JwtDecoder jwtDecoder(
            SecurityMetersService metersService, AuthTokenPersistencePort authTokenPersistencePort) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JwtSecurityConstants.JWT_ALGORITHM)
                .build();
        return token -> {
            try {
                var decodedToken = jwtDecoder.decode(token);
                if (authTokenPersistencePort.findByAccessToken(token).isEmpty()) {
                    throw new BadJwtException("Token revoked");
                }
                return decodedToken;
            } catch (Exception ex) {
                processJwtException(metersService, ex);
                throw ex;
            }
        };
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName(JwtSecurityConstants.AUTHORITIES_KEY);

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JwtSecurityConstants.JWT_ALGORITHM.getName());
    }

    private void processJwtException(SecurityMetersService metersService, Exception ex) {
        if (Strings.CI.contains(ex.getMessage(), "Invalid signature")) {
            metersService.trackTokenInvalidSignature();
        } else if (ex.getMessage().contains("Jwt expired at")) {
            metersService.trackTokenExpired();
        } else if (Strings.CI.contains(ex.getMessage(), "Token revoked")) {
            metersService.trackTokenMalformed();
        } else if (Strings.CI.contains(ex.getMessage(), "Invalid JWT serialization")
                || Strings.CI.contains(ex.getMessage(), "Malformed token")
                || Strings.CI.contains(ex.getMessage(), "Invalid unsecured/JWS/JWE")) {
            metersService.trackTokenMalformed();
        } else {
            log.error("Unknown JWT error {}", ex.getMessage());
        }
    }
}
