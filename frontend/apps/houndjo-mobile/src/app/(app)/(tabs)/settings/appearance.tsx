import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { Stack } from 'expo-router';
import { SymbolView, type SymbolViewProps } from 'expo-symbols';
import {
  appearancePreferencesFontEnum,
  appearancePreferencesThemeEnum,
  displayPreferencesTextSizeEnum,
  useGetCurrentUserPreferences,
  useUpdateCurrentUserPreferences,
  type AppearancePreferencesThemeEnumKey,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { ThemedText } from '@/components/themed-text';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { useAppTheme, type Theme } from '@/context/theme-provider';

const THEME_TO_API: Record<Theme, AppearancePreferencesThemeEnumKey> = {
  light: appearancePreferencesThemeEnum.LIGHT,
  dark: appearancePreferencesThemeEnum.DARK,
  system: appearancePreferencesThemeEnum.SYSTEM,
};

const THEME_ICONS: Record<Theme, SymbolViewProps['name']> = {
  system: { ios: 'circle.lefthalf.filled', android: 'contrast', web: 'contrast' },
  light: { ios: 'sun.max', android: 'light_mode', web: 'light_mode' },
  dark: { ios: 'moon.stars', android: 'dark_mode', web: 'dark_mode' },
};

const API_TO_THEME: Record<AppearancePreferencesThemeEnumKey, Theme> = {
  LIGHT: 'light',
  DARK: 'dark',
  SYSTEM: 'system',
};

export default function AppearanceScreen() {
  const { t } = useTranslation();
  const uiColors = useTheme();
  const { theme, setTheme } = useAppTheme();

  const { data: preferences } = useGetCurrentUserPreferences();
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useUpdateCurrentUserPreferences({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  // Keep the local theme in sync with the account's stored preference, e.g.
  // after a reinstall or on a second device.
  useEffect(() => {
    if (!preferences) return;
    const stored = API_TO_THEME[preferences.appearance.theme];
    if (stored !== theme) {
      setTheme(stored);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [preferences]);

  async function onSelect(next: Theme) {
    setFormError(null);
    setTheme(next);

    try {
      await mutateAsync({
        data: {
          appearance: {
            theme: THEME_TO_API[next],
            font: preferences?.appearance.font ?? appearancePreferencesFontEnum.SYSTEM,
          },
          // This mutation replaces the whole preferences document, so the
          // rest has to be carried through unchanged here.
          notifications: preferences?.notifications ?? { productUpdatesEnabled: false },
          display: preferences?.display ?? {
            textSize: displayPreferencesTextSizeEnum.DEFAULT,
            reduceMotion: false,
          },
        },
      });
    } catch {
      setFormError(t('settings.appearance.saveError'));
    }
  }

  const options: { value: Theme; label: string }[] = [
    { value: 'system', label: t('settings.appearance.system') },
    { value: 'light', label: t('settings.appearance.light') },
    { value: 'dark', label: t('settings.appearance.dark') },
  ];

  return (
    <>
      <Stack.Screen options={{ title: t('settings.nav.appearance') }} />
      <SettingsListScreen description={t('settings.appearance.description')}>
        <SettingsCard>
          <View style={styles.optionRow}>
            {options.map((option) => {
              const selected = option.value === theme;
              return (
                <Pressable
                  key={option.value}
                  accessibilityRole="button"
                  disabled={isPending}
                  onPress={() => void onSelect(option.value)}
                  style={[
                    styles.option,
                    {
                      backgroundColor: selected ? uiColors.text : uiColors.backgroundSelected,
                      borderColor: uiColors.backgroundSelected,
                    },
                  ]}>
                  <SymbolView
                    name={THEME_ICONS[option.value]}
                    size={18}
                    weight="medium"
                    tintColor={selected ? uiColors.background : uiColors.text}
                  />
                  <ThemedText
                    type="smallBold"
                    style={{ color: selected ? uiColors.background : uiColors.text }}>
                    {option.label}
                  </ThemedText>
                </Pressable>
              );
            })}
          </View>

          {formError && (
            <ThemedText type="small" themeColor="danger">
              {formError}
            </ThemedText>
          )}
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  optionRow: {
    flexDirection: 'row',
    gap: Spacing.two,
  },
  option: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.two,
    borderWidth: 1,
    borderRadius: Spacing.three,
    paddingVertical: Spacing.three,
  },
});
