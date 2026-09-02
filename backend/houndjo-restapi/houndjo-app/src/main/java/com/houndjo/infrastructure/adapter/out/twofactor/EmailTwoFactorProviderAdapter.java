package com.houndjo.infrastructure.adapter.out.twofactor;

import com.houndjo.config.ApplicationProperties;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.out.TwoFactorProviderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Email-based two-factor authentication provider implementing {@link TwoFactorProviderPort}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTwoFactorProviderAdapter implements TwoFactorProviderPort {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ApplicationProperties applicationProperties;
    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;
    private final HttpServletRequest request;

    @Override
    public TwoFactorMethodType getType() {
        return TwoFactorMethodType.EMAIL;
    }

    @Override
    public String generateAndSendCode(User user) {
        int codeLength = applicationProperties.getTwoFactor().codeLength();
        String code = generateNumericCode(codeLength);

        String email = user.getUserCredentials().getEmail();
        String firstName = user.getUserInfo().firstName();
        String languageKey = user.getUserInfo().languageKey();

        Locale locale = languageKey != null ? Locale.forLanguageTag(languageKey) : Locale.FRENCH;
        Context context = new Context(locale);
        context.setVariable("firstName", firstName);
        context.setVariable("code", code);
        context.setVariable("baseUrl", getBaseUrl());
        setCodeLifetimeVariables(context, locale);

        String content = templateEngine.process("mail/twoFactorCodeEmail", context);
        String subject = messageSource.getMessage("email.twofactor.title", null, locale);

        sendEmail(email, subject, content);

        return code;
    }

    @Override
    public boolean verify(User user, String storedCode, String providedCode) {
        return storedCode != null && storedCode.equals(providedCode);
    }

    private String generateNumericCode(int length) {
        int max = (int) Math.pow(10, length);
        int code = SECURE_RANDOM.nextInt(max);
        return String.format("%0" + length + "d", code);
    }

    private void setCodeLifetimeVariables(Context context, Locale locale) {
        long totalMinutes =
                applicationProperties.getTwoFactor().codeValidityPeriod().toMinutes();
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

        context.setVariable("codeLifetimeAmount", value);
        context.setVariable("codeLifetimeUnit", messageSource.getMessage(unitKey, null, locale));
    }

    private void sendEmail(String to, String subject, String content) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(applicationProperties.getMail().from());
            message.setSubject(subject);
            message.setText(content, true);
            javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            log.warn("2FA email could not be sent to '{}'", to, e);
        }
    }

    private String getBaseUrl() {
        return ObjectUtils.firstNonNull(request.getHeader(HttpHeaders.ORIGIN), "");
    }
}
