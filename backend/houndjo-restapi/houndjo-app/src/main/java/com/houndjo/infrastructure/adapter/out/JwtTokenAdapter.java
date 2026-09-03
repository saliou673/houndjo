package com.houndjo.infrastructure.adapter.out;

import com.houndjo.config.ApplicationProperties;
import com.houndjo.domain.exceptions.*;
import com.houndjo.domain.models.user.AuthenticatedUser;
import com.houndjo.domain.ports.out.JwtTokenPort;
import com.houndjo.infrastructure.security.JwtSecurityConstants;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Adapter implementing {@link com.houndjo.domain.ports.out.JwtTokenPort} using Spring Security OAuth2 JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenAdapter implements JwtTokenPort {

    private final JwtEncoder jwtEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final ApplicationProperties applicationProperties;

    @Override
    public AuthenticatedUser authenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

        try {
            Authentication authentication =
                    authenticationManagerBuilder.getObject().authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(StringUtils.SPACE));

            return new AuthenticatedUser(authentication.getName(), authorities);

        } catch (BadCredentialsException e) {
            log.error("Authentication failed: Invalid username or password", e);
            throw new InvalidCredentialsException();
        } catch (LockedException e) {
            log.error("Authentication failed: Account is locked", e);
            throw new AccountLockedException();
        } catch (DisabledException e) {
            log.error("Authentication failed: Account is disabled", e);
            throw new AccountDisabledException();
        } catch (AccountExpiredException e) {
            log.error("Authentication failed: Account has expired", e);
            throw new AccountExpiredException();
        } catch (CredentialsExpiredException e) {
            log.error("Authentication failed: Credentials have expired", e);
            throw new CredentialsExpiredException();
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            throw new AuthenticationFailedException(e.getMessage());
        }
    }

    @Override
    public String generateAccessToken(String email, String authorities, Instant expiryDate, Long activeOrganizationId) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiryDate)
                .subject(email)
                .claim(JwtSecurityConstants.AUTHORITIES_KEY, authorities);
        if (activeOrganizationId != null) {
            claimsBuilder.claim(ACTIVE_ORGANIZATION_CLAIM, activeOrganizationId);
        }
        JwtClaimsSet claims = claimsBuilder.build();

        JwsHeader jwsHeader = JwsHeader.with(JwtSecurityConstants.JWT_ALGORITHM).build();
        return this.jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue();
    }

    @Override
    public Instant calculateTokenValidity(boolean rememberMe) {
        Instant now = Instant.now();
        ApplicationProperties.Jwt jwtConfig = applicationProperties.getJwt();

        if (rememberMe) {
            return now.plus(jwtConfig.tokenValidityInSecondsForRememberMe(), ChronoUnit.SECONDS);
        }

        return now.plus(jwtConfig.tokenValidityInSeconds(), ChronoUnit.SECONDS);
    }
}
