package com.houndjo.domain.models.userpreference;

import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Appearance-related preferences selected by the user.
 */
@Getter
@NoArgsConstructor
public class AppearancePreferences {

    private ThemePreference theme;
    private FontPreference font;

    private AppearancePreferences(ThemePreference theme, FontPreference font) {
        this.theme = Objects.requireNonNull(theme, "theme must not be null");
        this.font = Objects.requireNonNull(font, "font must not be null");
    }

    public static AppearancePreferences of(ThemePreference theme, FontPreference font) {
        return new AppearancePreferences(theme, font);
    }

    public static AppearancePreferences defaultPreferences() {
        return new AppearancePreferences(ThemePreference.SYSTEM, FontPreference.INTER);
    }
}
