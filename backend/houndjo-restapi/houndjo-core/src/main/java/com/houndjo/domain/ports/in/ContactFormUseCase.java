package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.contact.ContactForm;

/**
 * Inbound port for handling contact form submissions.
 * <p>
 * Notifies the support team and acknowledges receipt to the sender.
 * The form is never persisted.
 */
public interface ContactFormUseCase {

    /**
     * Submits a contact form.
     * <p>
     * Triggers two asynchronous emails:
     * <ul>
     *   <li>One to the configured support address with the full form content.</li>
     *   <li>One to the sender confirming receipt and echoing the form content.</li>
     * </ul>
     *
     * @param contactForm the submitted contact form
     */
    void submit(ContactForm contactForm);
}
