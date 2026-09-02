package com.houndjo.infrastructure.security;

import com.houndjo.domain.exceptions.AccountNotActivatedException;
import com.houndjo.domain.exceptions.AccountNotFoundException;
import com.houndjo.domain.exceptions.EmailInvalidException;
import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.out.persistenceport.UserDetailsPersistencePort;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
@RequiredArgsConstructor
public class DomainUserDetailsService implements UserDetailsService {

    private final UserDetailsPersistencePort userDetailsPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@Nonnull final String email) {
        if (!(new EmailValidator().isValid(email, null))) {
            throw new EmailInvalidException(email);
        }

        return userDetailsPersistencePort
                .findUserWithAuthoritiesByEmail(email)
                .map(user -> createSpringSecurityUser(email, user))
                .orElseThrow(() -> new AccountNotFoundException(email));
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String email, User user) {
        if (!user.isActive()) {
            throw new AccountNotActivatedException(email);
        }

        List<SimpleGrantedAuthority> grantedAuthorities = user.resolvePermissions().stream()
                .map(Permission::code)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new AuthenticatedUser(
                user.getUserCredentials().getEmail(),
                user.getUserCredentials().getPasswordHash(),
                grantedAuthorities,
                user.getUserInfo().languageKey());
    }
}
