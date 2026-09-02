package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.models.contact.ContactForm;
import com.houndjo.domain.ports.in.ContactFormUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.ContactFormRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for submitting contact form messages.
 */
@RestController
@RequestMapping(path = "/api/contact", version = "1.0")
@RequiredArgsConstructor
public class ContactFormController {

    private final ContactFormUseCase contactFormUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendContactForm(@Valid @RequestBody ContactFormRequest request) {
        contactFormUseCase.submit(
                new ContactForm(request.name(), request.email(), request.subject(), request.message()));
    }
}
