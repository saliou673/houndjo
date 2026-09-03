import { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';
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

import { FormError } from '@/components/form-error';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { ThemedText } from '@/components/themed-text';
import { Button } from '@/components/ui/button';
import { Spacing } from '@/constants/theme';
import { useColor } from '@/hooks/useColor';
import { useModeToggle } from '@/hooks/useModeToggle';
import { type Mode } from '@/providers/mode-provider';

const THEME_TO_API: Record<Mode, AppearancePreferencesThemeEnumKey> = {
  light: appearancePreferencesThemeEnum.LIGHT,
  dark: appearancePreferencesThemeEnum.DARK,
  system: appearancePreferencesThemeEnum.SYSTEM,
};

const THEME_ICONS: Record<Mode, SymbolViewProps['name']> = {
  system: { ios: 'circle.lefthalf.filled', android: 'contrast', web: 'contrast' },
  light: { ios: 'sun.max', android: 'light_mode', web: 'light_mode' },
  dark: { ios: 'moon.stars', android: 'dark_mode', web: 'dark_mode' },
};

const API_TO_THEME: Record<AppearancePreferencesThemeEnumKey, Mode> = {
  LIGHT: 'light',
  DARK: 'dark',
  SYSTEM: 'system',
};

export default function AppearanceScreen() {
  const { t } = useTranslation();
  const primaryColor = useColor('primary');
  const primaryForegroundColor = useColor('primaryForeground');
  const { mode: theme, setMode: setTheme } = useModeToggle();

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

  async function onSelect(next: Mode) {
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

  const options: { value: Mode; label: string }[] = [
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
              const contentColor = selected ? primaryForegroundColor : primaryColor;
              return (
                <Button
                  key={option.value}
                  variant={selected ? 'default' : 'outline'}
                  disabled={isPending}
                  style={styles.option}
                  onPress={() => void onSelect(option.value)}>
                  <View style={styles.optionContent}>
                    <SymbolView
                      name={THEME_ICONS[option.value]}
                      size={18}
                      weight="medium"
                      tintColor={contentColor}
                    />
                    <ThemedText type="smallBold" style={{ color: contentColor }}>
                      {option.label}
                    </ThemedText>
                  </View>
                </Button>
              );
            })}
          </View>

          <FormError message={formError} />
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
  },
  optionContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.two,
  },
});
