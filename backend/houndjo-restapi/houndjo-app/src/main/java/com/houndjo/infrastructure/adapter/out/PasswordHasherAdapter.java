package com.houndjo.infrastructure.adapter.out;

import com.houndjo.domain.ports.out.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Adapter implementing {@link PasswordHasherPort} using Spring Security's {@link org.springframework.security.crypto.password.PasswordEncoder}.
 */
@Service
@RequiredArgsConstructor
public class PasswordHasherAdapter implements PasswordHasherPort {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
