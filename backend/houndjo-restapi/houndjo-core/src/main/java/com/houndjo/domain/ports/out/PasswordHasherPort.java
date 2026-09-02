package com.houndjo.domain.ports.out;

/**
 * Outbound port for hashing and verifying passwords.
 */
public interface PasswordHasherPort {

    /**
     * Hashes a plain-text password.
     *
     * @param rawPassword the plain-text password
     * @return the hashed password
     */
    String hash(String rawPassword);

    /**
     * Checks whether a raw password matches the stored hash.
     *
     * @param rawPassword    the plain-text password to check
     * @param hashedPassword the stored hash to compare against
     * @return {@code true} if the password matches
     */
    boolean matches(String rawPassword, String hashedPassword);
}
