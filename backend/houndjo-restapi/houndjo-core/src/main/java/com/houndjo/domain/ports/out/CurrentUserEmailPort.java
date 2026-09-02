package com.houndjo.domain.ports.out;

/**
 * Outbound port for resolving the currently authenticated user's email.
 */
public interface CurrentUserEmailPort {

    /**
     * Returns the email address of the currently authenticated user.
     *
     * @return the current user's email
     */
    String getCurrentUserEmail();
}
