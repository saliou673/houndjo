package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to initiate an email-change flow.
 *
 * @param newEmail the desired new email address
 */
public record EmailChangeRequest(
        @NotBlank(message = "New email is required") @Email(message = "New email must be a valid email address")
        String newEmail) {}
