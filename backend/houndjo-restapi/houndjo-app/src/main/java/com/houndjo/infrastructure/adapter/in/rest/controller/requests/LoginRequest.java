package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Login form request
 *
 * @param email
 * @param password
 * @param rememberMe
 */
public record LoginRequest(
        @NotNull @Size(min = 1, max = 50) String email,
        @NotNull @Size(min = 4, max = 100) String password,
        boolean rememberMe) {
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("email", email)
                .append("rememberMe", rememberMe)
                .toString(); // Do not add the password, it's sensible data
    }
}
