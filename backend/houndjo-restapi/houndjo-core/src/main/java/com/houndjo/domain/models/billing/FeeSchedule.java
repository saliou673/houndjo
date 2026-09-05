package com.houndjo.domain.models.billing;

import com.houndjo.domain.enumerations.FeeType;
import com.houndjo.domain.models.Auditable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate representing a fee schedule (registration or tuition) defined by an organization.
 * An organization may define several schedules, e.g. one per {@link FeeType}.
 */
@Getter
public class FeeSchedule extends Auditable<Long> {

    private final Long organizationId;
    private FeeType type;
    private BigDecimal amount;
    private String currencyCode;
    private String label;
    private boolean active;

    private FeeSchedule(
            Long id,
            Long organizationId,
            FeeType type,
            BigDecimal amount,
            String currencyCode,
            String label,
            boolean active,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.type = type;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.label = label;
        this.active = active;
    }

    public static FeeSchedule create(
            Long organizationId, FeeType type, BigDecimal amount, String currencyCode, String label) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        Objects.requireNonNull(label, "label must not be null");
        return new FeeSchedule(null, organizationId, type, amount, currencyCode, label, true, null, null, null);
    }

    public static FeeSchedule rehydrate(
            Long id,
            Long organizationId,
            FeeType type,
            BigDecimal amount,
            String currencyCode,
            String label,
            boolean active,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new FeeSchedule(
                id,
                organizationId,
                type,
                amount,
                currencyCode,
                label,
                active,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    public void update(FeeType type, BigDecimal amount, String currencyCode, String label, boolean active) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        Objects.requireNonNull(label, "label must not be null");
        this.type = type;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.label = label;
        this.active = active;
    }
}
