package com.houndjo.domain.models.userpreference;

/**
 * Notification-related preferences selected by the user.
 * <p>
 * Security-relevant emails are always sent and are not a preference; the
 * only real opt-in choice this app currently offers is product/marketing
 * updates.
 */
public record NotificationPreferences(boolean productUpdatesEnabled) {

    public static NotificationPreferences defaultPreferences() {
        return new NotificationPreferences(false);
    }
}
