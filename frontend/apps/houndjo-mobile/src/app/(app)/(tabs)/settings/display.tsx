import { useEffect } from 'react';
import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack } from 'expo-router';
import { SymbolView, type SymbolViewProps } from 'expo-symbols';
import {
  displayPreferencesTextSizeEnum,
  getCurrentUserPreferencesQueryKey,
  useGetCurrentUserPreferences,
  useUpdateCurrentUserPreferences,
  type DisplayPreferencesTextSizeEnumKey,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { ThemedText } from '@/components/themed-text';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { Switch } from '@/components/ui/switch';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { useColor } from '@/hooks/useColor';
import { useTextSize } from '@/context/text-size-provider';

const TEXT_SIZE_ICONS: Record<DisplayPreferencesTextSizeEnumKey, SymbolViewProps['name']> = {
  SMALL: { ios: 'textformat.size.smaller', android: 'text_decrease', web: 'text_decrease' },
  DEFAULT: { ios: 'textformat.size', android: 'format_size', web: 'format_size' },
  LARGE: { ios: 'textformat.size.larger', android: 'text_increase', web: 'text_increase' },
};

// Web's display-form.tsx used to be about which items show in a desktop
// sidebar (recents/home/applications/desktop/...), which has no mobile
// equivalent - there's no sidebar. Both platforms now share this schema
// instead: text size and reduced motion, meaningful everywhere.
export default function DisplayScreen() {
  const { t } = useTranslation();
  const primaryColor = useColor('primary');
  const primaryForegroundColor = useColor('primaryForeground');
  const queryClient = useQueryClient();
  const { textSize, setTextSize } = useTextSize();
  const { data: preferences, isLoading, isError } = useGetCurrentUserPreferences();

  // Separate mutation instances so toggling one control's `isPending` (which
  // drives its `disabled` prop) doesn't also flip the other control's -
  // sharing one mutation made the reduce-motion Switch flash disabled/enabled
  // every time a text size button was pressed, and vice versa.
  const { mutate: updateTextSize, isPending: isTextSizePending } =
    useUpdateCurrentUserPreferences({
      mutation: { meta: { skipGlobalErrorToast: true } },
    });
  const { mutate: updateReduceMotion, isPending: isReduceMotionPending } =
    useUpdateCurrentUserPreferences({
      mutation: { meta: { skipGlobalErrorToast: true } },
    });

  // Keep the local text size in sync with the account's stored preference,
  // e.g. after a reinstall or on a second device - same as AppThemeProvider
  // does for theme in appearance.tsx.
  useEffect(() => {
    if (!preferences) return;
    if (preferences.display.textSize !== textSize) {
      setTextSize(preferences.display.textSize);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [preferences]);

  function onSaveError() {
    showToast(t('settings.display.saveError'), 'error');
  }

  function onSaveSuccess() {
    void queryClient.invalidateQueries({ queryKey: getCurrentUserPreferencesQueryKey() });
  }

  function onTextSizeChange(textSize: DisplayPreferencesTextSizeEnumKey) {
    if (!preferences) return;

    setTextSize(textSize);
    updateTextSize(
      {
        data: {
          appearance: preferences.appearance,
          notifications: preferences.notifications,
          display: { ...preferences.display, textSize },
        },
      },
      { onError: onSaveError, onSuccess: onSaveSuccess }
    );
  }

  function onReduceMotionChange(reduceMotion: boolean) {
    if (!preferences) return;

    updateReduceMotion(
      {
        data: {
          appearance: preferences.appearance,
          notifications: preferences.notifications,
          display: { ...preferences.display, reduceMotion },
        },
      },
      { onError: onSaveError, onSuccess: onSaveSuccess }
    );
  }

  const textSizeOptions: { value: DisplayPreferencesTextSizeEnumKey; label: string }[] = [
    { value: displayPreferencesTextSizeEnum.SMALL, label: t('settings.display.textSizeSmall') },
    {
      value: displayPreferencesTextSizeEnum.DEFAULT,
      label: t('settings.display.textSizeDefault'),
    },
    { value: displayPreferencesTextSizeEnum.LARGE, label: t('settings.display.textSizeLarge') },
  ];

  return (
    <>
      <Stack.Screen options={{ title: t('settings.nav.display') }} />
      <SettingsListScreen>
        {isLoading ? (
          <Spinner />
        ) : isError || !preferences ? (
          <ThemedText themeColor="danger">{t('settings.display.loadError')}</ThemedText>
        ) : (
          <SettingsCard style={styles.card}>
            <View style={styles.field}>
              <ThemedText type="smallBold">{t('settings.display.textSizeLabel')}</ThemedText>
              <ThemedText type="small" themeColor="textSecondary">
                {t('settings.display.textSizeDescription')}
              </ThemedText>
              <View style={styles.optionRow}>
                {textSizeOptions.map((option) => {
                  const selected = option.value === textSize;
                  const contentColor = selected ? primaryForegroundColor : primaryColor;
                  return (
                    <Button
                      key={option.value}
                      variant={selected ? 'default' : 'outline'}
                      disabled={isTextSizePending}
                      style={styles.option}
                      onPress={() => onTextSizeChange(option.value)}>
                      <View style={styles.optionContent}>
                        <SymbolView
                          name={TEXT_SIZE_ICONS[option.value]}
                          size={18}
                          weight="medium"
                          tintColor={contentColor}
                        />
                        <ThemedText type="small" style={{ color: contentColor }}>
                          {option.label}
                        </ThemedText>
                      </View>
                    </Button>
                  );
                })}
              </View>
            </View>

            <View style={styles.switchRow}>
              <View style={styles.switchLabel}>
                <ThemedText>{t('settings.display.reduceMotionLabel')}</ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  {t('settings.display.reduceMotionDescription')}
                </ThemedText>
              </View>
              <Switch
                value={preferences.display.reduceMotion}
                disabled={isReduceMotionPending}
                onValueChange={onReduceMotionChange}
              />
            </View>
          </SettingsCard>
        )}
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: Spacing.four,
  },
  field: {
    gap: Spacing.two,
  },
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
  switchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.three,
  },
  switchLabel: {
    flex: 1,
    gap: Spacing.half,
  },
});
