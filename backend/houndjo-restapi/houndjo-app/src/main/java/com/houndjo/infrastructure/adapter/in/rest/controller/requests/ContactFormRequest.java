package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request carrying the data submitted via the public contact form.
 *
 * @param name    sender's full name
 * @param email   sender's email address
 * @param subject message subject
 * @param message message body
 */
public record ContactFormRequest(
        @NotBlank @Size(max = 100) String name,

        @NotBlank @Email @Size(max = 255) String email,

        @NotBlank @Size(max = 150) String subject,

        @NotBlank @Size(max = 5000) String message) {}
