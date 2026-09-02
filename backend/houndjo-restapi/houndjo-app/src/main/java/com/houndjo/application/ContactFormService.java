package com.houndjo.application;

import com.houndjo.domain.models.contact.ContactForm;
import com.houndjo.domain.ports.in.ContactFormUseCase;
import com.houndjo.domain.ports.out.NotificationSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service implementing {@link ContactFormUseCase}: dispatches contact form submissions via notifications.
 */
@Service
@RequiredArgsConstructor
public class ContactFormService implements ContactFormUseCase {

    private final NotificationSenderPort notificationSenderPort;

    @Override
    public void submit(ContactForm contactForm) {
        notificationSenderPort.sendContactFormToAdmin(contactForm);
        notificationSenderPort.sendContactFormConfirmationToUser(contactForm);
    }
}
