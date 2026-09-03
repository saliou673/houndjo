package com.houndjo.infrastructure.adapter.out.notification;

import com.houndjo.config.ApplicationProperties;
import com.houndjo.domain.models.contact.ContactForm;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserCredentials;
import com.houndjo.domain.models.user.UserInfo;
import com.houndjo.domain.ports.out.NotificationSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Email adapter implementing {@link NotificationSenderPort} using Jakarta Mail and Thymeleaf templates.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationAdapterPort implements NotificationSenderPort {
    private static final String USER = "user";
    private static final String BASE_URL = "baseUrl";
    private static final String ACCOUNT_VALIDATION_ROUTE = "accountValidationRoute";
    private static final String LOGIN_ROUTE = "loginRoute";
    private static final String RESET_PASSWORD_ROUTE = "resetPasswordRoute";
    private static final String RECOVERY_PERIOD_DAYS = "recoveryPeriodDays";
    private static final String CODE_LIFETIME_AMOUNT = "codeLifetimeAmount";
    private static final String CODE_LIFETIME_UNIT = "codeLifetimeUnit";
    private static final String MANAGED_USER_INVITATION_ROUTE = "managedUserInvitationRoute";
    private static final String CONTACT_FORM = "contactForm";
    private static final String PLAN_TITLE = "planTitle";
    private static final String SUBSCRIPTION = "subscription";

    private final ApplicationProperties applicationProperties;
    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    @Override
    @Async
    public void sendOrganizationInvitationNotification(String email, String invitationCode, String languageKey) {
        if (StringUtils.isBlank(email)) return;
        Locale locale = Locale.forLanguageTag(StringUtils.defaultIfBlank(languageKey, "fr"));
        String subject = messageSource.getMessage("email.organization-invitation.title", null, "Invitation", locale);
        String body = messageSource.getMessage(
                "email.organization-invitation.body",
                new Object[] {invitationCode},
                "You have been invited to join an organization. Invitation code: {0}",
                locale);
        sendEmailSync(email, subject, body);
    }

    @Override
    @Async
    public void sendActivationNotification(User user) {
        String templateName = "mail/activationEmail";
        String titleKey = "email.activation.title";
        String email = user.getUserCredentials().getEmail();
        if (StringUtils.isBlank(email)) {
            log.debug("Email doesn't exist for user '{}'", email);
            return;
        }
        Locale locale = Locale.forLanguageTag("fr");
        Context context = new Context(locale);

        NotificationRecipient notificationRecipient = getNotificationRecipient(user);

        context.setVariable(USER, notificationRecipient);
        ApplicationProperties.Mail mail = applicationProperties.getMail();
        context.setVariable(BASE_URL, getBaseUrl());
        context.setVariable(ACCOUNT_VALIDATION_ROUTE, mail.routes().accountValidation());
        context.setVariable(RESET_PASSWORD_ROUTE, mail.routes().resetPassword());
        context.setVariable(LOGIN_ROUTE, mail.routes().resetPassword());
        setCodeLifetimeVariables(context, locale);
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        this.sendEmailSync(email, subject, content);
    }

    private NotificationRecipient getNotificationRecipient(User user) {
        UserCredentials userCredentials = user.getUserCredentials();
        UserInfo userInfo = user.getUserInfo();
        return new NotificationRecipient(
                userInfo.firstName(),
                userCredentials.getActivationCode(),
                userCredentials.getResetCode(),
                userCredentials.getEmail(),
                userInfo.phoneNumber(),
                userInfo.languageKey());
    }

    @Override
    @Async
    public void sendCreationNotification(User user) {
        this.sendEmailFromTemplateSync(user, "mail/creationEmail", "email.activation.title");
    }

    @Override
    @Async
    public void sendPasswordResetNotification(User user) {
        this.sendEmailFromTemplateSync(user, "mail/passwordResetEmail", "email.reset.title");
    }

    @Override
    @Async
    public void sendAccountDeletionNotification(User user) {
        this.sendEmailFromTemplateSync(user, "mail/accountDeletionEmail", "email.deletion.title");
    }

    @Override
    @Async
    public void sendManagedUserInvitationNotification(User user) {
        NotificationRecipient notificationRecipient = getNotificationRecipient(user);
        String email = notificationRecipient.email();

        if (StringUtils.isBlank(email)) {
            log.debug("Email doesn't exist for user '{}'", email);
            return;
        }
        Locale locale = Locale.forLanguageTag(notificationRecipient.languageKey());
        Context context = new Context(locale);
        context.setVariable(USER, notificationRecipient);
        ApplicationProperties.Mail mail = applicationProperties.getMail();
        context.setVariable(BASE_URL, getBaseUrl());
        context.setVariable(MANAGED_USER_INVITATION_ROUTE, mail.routes().managedUserInvitation());
        setInvitationCodeLifetimeVariables(context, locale);
        String content = templateEngine.process("mail/managedUserInvitationEmail", context);
        String subject = messageSource.getMessage("email.invitation.title", null, locale);
        this.sendEmailSync(email, subject, content);
    }

    private void sendEmailSync(String to, String subject, String content) {
        this.sendEmailSync(to, subject, content, null, null);
    }

    private void sendEmailSync(
            String to, String subject, String content, byte[] attachmentContent, String attachmentFilename) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message =
                    new MimeMessageHelper(mimeMessage, attachmentContent != null, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(applicationProperties.getMail().from());
            message.setSubject(subject);
            message.setText(content, true);
            if (attachmentContent != null && attachmentFilename != null) {
                message.addAttachment(attachmentFilename, new ByteArrayResource(attachmentContent));
            }
            javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            log.warn("Email could not be sent to user '{}'", to, e);
        }
    }

    private void sendEmailFromTemplateSync(User user, String templateName, String titleKey) {
        NotificationRecipient notificationRecipient = getNotificationRecipient(user);
        String email = notificationRecipient.email();

        if (StringUtils.isBlank(email)) {
            log.debug("Email doesn't exist for user '{}'", email);
            return;
        }
        Locale locale = Locale.forLanguageTag(notificationRecipient.languageKey());
        Context context = new Context(locale);
        context.setVariable(USER, notificationRecipient);
        ApplicationProperties.Mail mail = applicationProperties.getMail();
        context.setVariable(BASE_URL, getBaseUrl());
        context.setVariable(ACCOUNT_VALIDATION_ROUTE, mail.routes().accountValidation());
        context.setVariable(RESET_PASSWORD_ROUTE, mail.routes().resetPassword());
        context.setVariable(LOGIN_ROUTE, mail.routes().resetPassword());
        context.setVariable(
                RECOVERY_PERIOD_DAYS,
                applicationProperties
                        .getAccount()
                        .softDeletedUserRetentionPeriod()
                        .toDays());
        setCodeLifetimeVariables(context, locale);
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        this.sendEmailSync(email, subject, content);
    }

    private void setCodeLifetimeVariables(Context context, Locale locale) {
        long totalMinutes =
                applicationProperties.getAccount().resetCodeValidityPeriod().toMinutes();
        long value;
        String unitKey;

        if (totalMinutes % (7 * 24 * 60) == 0) {
            value = totalMinutes / (7 * 24 * 60);
            unitKey = value == 1 ? "duration.unit.week" : "duration.unit.weeks";
        } else if (totalMinutes % (24 * 60) == 0) {
            value = totalMinutes / (24 * 60);
            unitKey = value == 1 ? "duration.unit.day" : "duration.unit.days";
        } else if (totalMinutes % 60 == 0) {
            value = totalMinutes / 60;
            unitKey = value == 1 ? "duration.unit.hour" : "duration.unit.hours";
        } else {
            value = totalMinutes;
            unitKey = value == 1 ? "duration.unit.minute" : "duration.unit.minutes";
        }

        context.setVariable(CODE_LIFETIME_AMOUNT, value);
        context.setVariable(CODE_LIFETIME_UNIT, messageSource.getMessage(unitKey, null, locale));
    }

    private void setInvitationCodeLifetimeVariables(Context context, Locale locale) {
        long totalMinutes = applicationProperties
                .getAccount()
                .managedUserInvitationCodeValidityPeriod()
                .toMinutes();
        long value;
        String unitKey;

        if (totalMinutes % (7 * 24 * 60) == 0) {
            value = totalMinutes / (7 * 24 * 60);
            unitKey = value == 1 ? "duration.unit.week" : "duration.unit.weeks";
        } else if (totalMinutes % (24 * 60) == 0) {
            value = totalMinutes / (24 * 60);
            unitKey = value == 1 ? "duration.unit.day" : "duration.unit.days";
        } else if (totalMinutes % 60 == 0) {
            value = totalMinutes / 60;
            unitKey = value == 1 ? "duration.unit.hour" : "duration.unit.hours";
        } else {
            value = totalMinutes;
            unitKey = value == 1 ? "duration.unit.minute" : "duration.unit.minutes";
        }

        context.setVariable(CODE_LIFETIME_AMOUNT, value);
        context.setVariable(CODE_LIFETIME_UNIT, messageSource.getMessage(unitKey, null, locale));
    }

    @Override
    @Async
    public void sendContactFormToAdmin(ContactForm contactForm) {
        Locale locale = Locale.forLanguageTag("fr");
        Context context = new Context(locale);
        context.setVariable(CONTACT_FORM, contactForm);
        String content = templateEngine.process("mail/contactFormAdminEmail", context);
        String subject =
                messageSource.getMessage("email.contact.admin.title", new Object[] {contactForm.subject()}, locale);
        applicationProperties
                .getContact()
                .recipientEmails()
                .forEach(recipient -> this.sendEmailSync(recipient, subject, content));
    }

    @Override
    @Async
    public void sendContactFormConfirmationToUser(ContactForm contactForm) {
        Locale locale = Locale.forLanguageTag("fr");
        Context context = new Context(locale);
        context.setVariable(CONTACT_FORM, contactForm);
        String content = templateEngine.process("mail/contactFormUserEmail", context);
        String subject = messageSource.getMessage("email.contact.user.title", null, locale);
        this.sendEmailSync(contactForm.senderEmail(), subject, content);
    }

    @Override
    @Async
    public void sendEmailChangeOtpNotification(User user, String newEmail) {
        UserInfo userInfo = user.getUserInfo();
        String firstName = userInfo.firstName();
        String emailChangeCode = user.getUserCredentials().getEmailChangeCode();
        String languageKey = userInfo.languageKey();

        if (StringUtils.isBlank(newEmail)) {
            log.debug("New email is blank, cannot send email change OTP");
            return;
        }

        Locale locale = resolveLocale(languageKey);
        Context context = new Context(locale);
        context.setVariable("firstName", firstName);
        context.setVariable("code", emailChangeCode);
        context.setVariable(BASE_URL, getBaseUrl());
        setCodeLifetimeVariables(context, locale);
        String content = templateEngine.process("mail/emailChangeOtpEmail", context);
        String subject = messageSource.getMessage("email.email-change.title", null, locale);
        this.sendEmailSync(newEmail, subject, content);
    }

    @Override
    @Async
    public void sendEmailChangedOldAddressNotification(User user, String oldEmail) {
        if (StringUtils.isBlank(oldEmail)) {
            log.debug("Old email is blank, cannot send email-changed security notice");
            return;
        }
        UserInfo userInfo = user.getUserInfo();
        Locale locale = resolveLocale(userInfo.languageKey());
        Context context = new Context(locale);
        context.setVariable("firstName", userInfo.firstName());
        context.setVariable("newEmail", user.getUserCredentials().getEmail());
        context.setVariable(BASE_URL, getBaseUrl());
        String content = templateEngine.process("mail/emailChangedOldAddressEmail", context);
        String subject = messageSource.getMessage("email.email-changed-old.title", null, locale);
        this.sendEmailSync(oldEmail, subject, content);
    }

    @Override
    @Async
    public void sendEmailChangedNewAddressNotification(User user) {
        NotificationRecipient recipient = getNotificationRecipient(user);
        String newEmail = recipient.email();
        if (StringUtils.isBlank(newEmail)) {
            log.debug("New email is blank, cannot send email-confirmed notice");
            return;
        }
        Locale locale = resolveLocale(recipient.languageKey());
        Context context = new Context(locale);
        context.setVariable("firstName", recipient.firstName());
        context.setVariable(BASE_URL, getBaseUrl());
        String content = templateEngine.process("mail/emailChangedNewAddressEmail", context);
        String subject = messageSource.getMessage("email.email-changed-new.title", null, locale);
        this.sendEmailSync(newEmail, subject, content);
    }

    public String getBaseUrl() {
        if (StringUtils.isNotBlank(applicationProperties.getWebAppOrigin())) {
            return applicationProperties.getWebAppOrigin();
        }

        HttpServletRequest request = requestProvider.getIfAvailable();
        return request == null ? "" : ObjectUtils.firstNonNull(request.getHeader(HttpHeaders.ORIGIN), "");
    }

    private Locale resolveLocale(String languageKey) {
        return StringUtils.isBlank(languageKey) ? Locale.forLanguageTag("fr") : Locale.forLanguageTag(languageKey);
    }
}
