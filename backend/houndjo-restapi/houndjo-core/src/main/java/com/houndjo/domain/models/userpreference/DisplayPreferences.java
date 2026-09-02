package com.houndjo.domain.models.userpreference;

/**
 * Display-related preferences selected by the user.
 * <p>
 * Deliberately not the sidebar-item visibility toggles the original web
 * mockup shipped with (recents/home/desktop/...) - those describe a desktop
 * sidebar layout with no mobile equivalent. Text size and reduced motion are
 * the two display settings that are meaningful on every platform this app
 * ships to.
 */
public record DisplayPreferences(TextSizePreference textSize, boolean reduceMotion) {

    public static DisplayPreferences defaultPreferences() {
        return new DisplayPreferences(TextSizePreference.DEFAULT, false);
    }
}
