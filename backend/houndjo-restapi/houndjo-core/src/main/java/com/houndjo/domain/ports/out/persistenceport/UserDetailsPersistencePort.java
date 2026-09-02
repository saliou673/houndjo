package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.user.User;
import java.util.Optional;

/**
 * Persistence port used by Spring Security to load user details for authentication.
 */
public interface UserDetailsPersistencePort {

    /**
     * Finds the user with their full authority set by email.
     *
     * @param email the user's email address
     * @return the matching user, or empty if not found
     */
    Optional<User> findUserWithAuthoritiesByEmail(String email);
}
