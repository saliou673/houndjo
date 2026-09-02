package com.houndjo.domain.models.contact;

/**
 * Domain model representing a contact form submission.
 * Not persisted — used only to carry data through the notification flow.
 */
public record ContactForm(String senderName, String senderEmail, String subject, String message) {}
