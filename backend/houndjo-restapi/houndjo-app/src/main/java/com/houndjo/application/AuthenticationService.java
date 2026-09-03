package com.houndjo.application;

import com.houndjo.config.ApplicationProperties;
import com.houndjo.domain.exceptions.InvalidRefreshTokenException;
import com.houndjo.domain.exceptions.TwoFactorSetupRequiredException;
import com.houndjo.domain.exceptions.UserNotFoundException;
import com.houndjo.domain.models.auth.*;
import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.domain.models.securitysettings.SecuritySettings;
import com.houndjo.domain.models.user.AuthenticatedUser;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.in.AuthenticationUseCase;
import com.houndjo.domain.ports.out.JwtTokenPort;
import com.houndjo.domain.ports.out.TwoFactorProviderPort;
import com.houndjo.domain.ports.out.persistenceport.AuthTokenPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.MembershipPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.SecuritySettingsPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.TwoFactorChallengePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.UserPersistencePort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link AuthenticationUseCase}: login, token refresh, and logout.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationService implements AuthenticationUseCase {

    private final JwtTokenPort jwtTokenPort;
    private final AuthTokenPersistencePort authTokenPersistencePort;
    private final MembershipPersistencePort membershipPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final TwoFactorChallengePersistencePort twoFactorChallengePersistencePort;
    private final List<TwoFactorProviderPort> twoFactorProviders;
    private final ApplicationProperties applicationProperties;
    private final SecuritySettingsPersistencePort securitySettingsPersistencePort;

    @Override
    public LoginResult login(String email, String password, boolean rememberMe) {
        AuthenticatedUser authenticatedUser = jwtTokenPort.authenticate(email, password);

        User user = userPersistencePort.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        boolean globalTwoFactorRequired = securitySettingsPersistencePort
                .find()
                .map(SecuritySettings::isTwoFactorRequired)
                .orElse(false);

        if (globalTwoFactorRequired && !user.isTwoFactorEnabled()) {
            throw new TwoFactorSetupRequiredException();
        }

        if (user.isTwoFactorEnabled()) {
            return createTwoFactorChallenge(user, rememberMe);
        }

        return completeLogin(authenticatedUser, email, rememberMe);
    }

    @Override
    public JwtToken refreshToken(String refreshTokenValue) {
        AuthToken authToken = authTokenPersistencePort
                .findByRefreshToken(refreshTokenValue)
                .filter(token -> token.isValid(Instant.now()))
                .orElseThrow(InvalidRefreshTokenException::new);

        User user = authToken.getUser();

        String authorities =
                user.resolvePermissions().stream().map(Permission::code).collect(Collectors.joining(" "));

        Instant newExpiryDate = jwtTokenPort.calculateTokenValidity(authToken.getRememberMe());

        String accessToken = jwtTokenPort.generateAccessToken(
                user.getUserCredentials().getEmail(), authorities, newExpiryDate, defaultOrganizationId(user));

        authToken.updateAccessToken(accessToken);
        authToken.updateExpiryDate(newExpiryDate.plus(30, ChronoUnit.DAYS));
        authTokenPersistencePort.save(authToken);

        return new JwtToken(accessToken, refreshTokenValue);
    }

    @Override
    public void logout(String accessToken) {
        authTokenPersistencePort.deleteByAccessToken(accessToken);
    }

    public LoginResult.Complete completeLogin(AuthenticatedUser authenticatedUser, String email, boolean rememberMe) {
        User user = userPersistencePort.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        Instant expiryDate = jwtTokenPort.calculateTokenValidity(rememberMe);
        String accessToken = jwtTokenPort.generateAccessToken(
                authenticatedUser.email(), authenticatedUser.authorities(), expiryDate, defaultOrganizationId(user));
        String refreshToken = generateAndSaveRefreshToken(email, accessToken, expiryDate, rememberMe);
        return new LoginResult.Complete(new JwtToken(accessToken, refreshToken));
    }

    private LoginResult.TwoFactorRequired createTwoFactorChallenge(User user, boolean rememberMe) {
        TwoFactorMethodType method = user.getTwoFactorMethod();
        TwoFactorProviderPort provider = getProvider(method);

        String code = provider.generateAndSendCode(user);

        Instant expiryDate =
                Instant.now().plus(applicationProperties.getTwoFactor().codeValidityPeriod());

        twoFactorChallengePersistencePort.deleteByUserId(user.getId());

        TwoFactorChallenge challenge = TwoFactorChallenge.create(
                UUID.randomUUID().toString(),
                user,
                code,
                method,
                TwoFactorChallengePurpose.LOGIN,
                rememberMe,
                expiryDate);
        TwoFactorChallenge saved = twoFactorChallengePersistencePort.save(challenge);

        return new LoginResult.TwoFactorRequired(saved.getId(), method);
    }

    private TwoFactorProviderPort getProvider(TwoFactorMethodType type) {
        Map<TwoFactorMethodType, TwoFactorProviderPort> providerMap = twoFactorProviders.stream()
                .collect(Collectors.toMap(TwoFactorProviderPort::getType, Function.identity()));
        TwoFactorProviderPort provider = providerMap.get(type);
        if (provider == null) {
            throw new IllegalStateException("No 2FA provider found for type: " + type);
        }
        return provider;
    }

    private String generateAndSaveRefreshToken(
            String email, String accessToken, Instant accessTokenExpiryDate, boolean rememberMe) {
        User user = userPersistencePort.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        authTokenPersistencePort.deleteAllForUser(user);

        String tokenValue = UUID.randomUUID().toString();
        Instant expiryDate = accessTokenExpiryDate.plus(30, ChronoUnit.DAYS);

        AuthToken authToken = AuthToken.create(accessToken, tokenValue, rememberMe, expiryDate, user);

        authTokenPersistencePort.save(authToken);
        return tokenValue;
    }

    private Long defaultOrganizationId(User user) {
        return membershipPersistencePort.findActiveByUserId(user.getId()).stream()
                .map(membership -> membership.getOrganizationId())
                .findFirst()
                .orElse(null);
    }
}
