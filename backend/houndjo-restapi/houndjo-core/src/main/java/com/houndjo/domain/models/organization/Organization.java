package com.houndjo.domain.models.organization;

import com.houndjo.domain.constants.DomainConstants;
import com.houndjo.domain.enumerations.OrganizationStatus;
import com.houndjo.domain.models.Auditable;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * Aggregate representing an organization (a Quranic school) using the platform.
 */
@Getter
public class Organization extends Auditable<Long> {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final String FALLBACK_SLUG = "school";

    private String name;
    private String slug;
    private String contactEmail;
    private String phoneNumber;
    private String address;
    private String defaultCurrencyCode;
    private String defaultLanguageKey;
    private String timezone;
    private OrganizationStatus status;

    private Organization(
            Long id,
            String name,
            String slug,
            String contactEmail,
            String phoneNumber,
            String address,
            String defaultCurrencyCode,
            String defaultLanguageKey,
            String timezone,
            OrganizationStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.name = name;
        this.slug = slug;
        this.contactEmail = contactEmail;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.defaultCurrencyCode = defaultCurrencyCode;
        this.defaultLanguageKey = defaultLanguageKey;
        this.timezone = timezone;
        this.status = status;
    }

    public static Organization create(
            String name,
            String contactEmail,
            String phoneNumber,
            String address,
            String defaultCurrencyCode,
            String defaultLanguageKey) {
        return new Organization(
                null,
                name,
                slugify(name),
                contactEmail,
                phoneNumber,
                address,
                defaultIfBlank(defaultCurrencyCode, DomainConstants.DEFAULT_CURRENCY_CODE),
                defaultIfBlank(defaultLanguageKey, DomainConstants.DEFAULT_LANGUAGE),
                DomainConstants.DEFAULT_TIMEZONE,
                OrganizationStatus.ACTIVE,
                null,
                null,
                null);
    }

    public static Organization rehydrate(
            Long id,
            String name,
            String slug,
            String contactEmail,
            String phoneNumber,
            String address,
            String defaultCurrencyCode,
            String defaultLanguageKey,
            String timezone,
            OrganizationStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new Organization(
                id,
                name,
                slug,
                contactEmail,
                phoneNumber,
                address,
                defaultCurrencyCode,
                defaultLanguageKey,
                timezone,
                status,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    /**
     * Derives a kebab-case slug candidate from a name. Does not guarantee uniqueness — the
     * caller is responsible for resolving collisions before persisting.
     */
    public static String slugify(String name) {
        String decomposed = Normalizer.normalize(name, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
        String withoutDiacritics = DIACRITICS.matcher(decomposed).replaceAll("");
        String normalized = NON_ALPHANUMERIC.matcher(withoutDiacritics).replaceAll("-");
        int start = 0;
        int end = normalized.length();
        while (start < end && normalized.charAt(start) == '-') {
            start++;
        }
        while (end > start && normalized.charAt(end - 1) == '-') {
            end--;
        }
        String slug = normalized.substring(start, end);
        return slug.isEmpty() ? FALLBACK_SLUG : slug;
    }

    /**
     * Overwrites the slug, used by the registration flow to resolve a collision-free slug
     * before the first save.
     */
    public void assignSlug(String slug) {
        this.slug = slug;
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
