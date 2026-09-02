import { useEffect, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import Constants from 'expo-constants';
import { Image } from 'expo-image';
import { SymbolView } from 'expo-symbols';
import { Platform, Pressable, ScrollView, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { SettingsCard } from '@/components/settings-card';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { WebBadge } from '@/components/web-badge';
import { BottomTabInset, MaxContentWidth, Radius, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

// Mirrors BRAND_COLOR in app.config.ts - the default a fork starts with
// before picking one of the presets below.
const DEFAULT_ACCENT = '#0f172b';

const ACCENT_PRESETS = [
  { value: DEFAULT_ACCENT, label: 'Default' },
  { value: '#2563eb', label: 'Blue' },
  { value: '#059669', label: 'Green' },
  { value: '#d97706', label: 'Amber' },
  { value: '#dc2626', label: 'Red' },
  { value: '#7c3aed', label: 'Violet' },
] as const;

const ACCENT_STORAGE_KEY = 'houndjo-mobile-customize-accent';

export default function CustomizeScreen() {
  const safeAreaInsets = useSafeAreaInsets();
  const insets = {
    ...safeAreaInsets,
    bottom: safeAreaInsets.bottom + BottomTabInset + Spacing.three,
  };
  const theme = useTheme();
  const appName = Constants.expoConfig?.name ?? 'Houndjo';
  const [accent, setAccent] = useState<string>(DEFAULT_ACCENT);

  useEffect(() => {
    AsyncStorage.getItem(ACCENT_STORAGE_KEY).then((stored) => {
      if (stored) setAccent(stored);
    });
  }, []);

  function onSelectAccent(value: string) {
    setAccent(value);
    void AsyncStorage.setItem(ACCENT_STORAGE_KEY, value);
  }

  const contentPlatformStyle = Platform.select({
    android: {
      paddingTop: insets.top,
      paddingLeft: insets.left,
      paddingRight: insets.right,
      paddingBottom: insets.bottom,
    },
    web: {
      paddingTop: Spacing.six,
      paddingBottom: Spacing.four,
    },
  });

  return (
    <ScrollView
      style={[styles.scrollView, { backgroundColor: theme.background }]}
      contentInset={insets}
      contentContainerStyle={[styles.contentContainer, contentPlatformStyle]}>
      <ThemedView style={styles.container}>
        <ThemedView style={styles.titleContainer}>
          <ThemedText type="subtitle">Customize</ThemedText>
          <ThemedText style={styles.centerText} themeColor="textSecondary">
            A starting point for re-skinning this app for your own product.{'\n'}Swap the icon
            assets and app name below, and pick an accent color to preview.
          </ThemedText>
        </ThemedView>

        <ThemedView style={styles.sectionsWrapper}>
          <SettingsCard style={styles.identityCard}>
            <Image source={require('@/assets/images/icon.png')} style={styles.appIcon} />
            <ThemedView style={styles.identityText}>
              <ThemedText type="smallBold">{appName}</ThemedText>
              <ThemedText type="small" themeColor="textSecondary">
                Name and icon come from <ThemedText type="code">app.config.ts</ThemedText> and{' '}
                <ThemedText type="code">assets/images/icon.png</ThemedText>.
              </ThemedText>
            </ThemedView>
          </SettingsCard>

          <ThemedView style={styles.section}>
            <ThemedText type="smallBold" themeColor="textSecondary" style={styles.sectionTitle}>
              Accent color
            </ThemedText>

            <SettingsCard style={styles.accentCard}>
              <ThemedView style={styles.swatchRow}>
                {ACCENT_PRESETS.map((preset) => {
                  const selected = preset.value === accent;
                  return (
                    <Pressable
                      key={preset.value}
                      accessibilityRole="button"
                      accessibilityLabel={preset.label}
                      onPress={() => onSelectAccent(preset.value)}
                      style={({ pressed }) => pressed && styles.pressed}>
                      <ThemedView
                        style={[
                          styles.swatch,
                          { backgroundColor: preset.value },
                          selected && { borderColor: theme.text },
                        ]}>
                        {selected && (
                          <SymbolView
                            name={{ ios: 'checkmark', android: 'check', web: 'check' }}
                            size={16}
                            weight="bold"
                            tintColor="#ffffff"
                          />
                        )}
                      </ThemedView>
                    </Pressable>
                  );
                })}
              </ThemedView>

              <ThemedView style={[styles.previewButton, { backgroundColor: accent }]}>
                <ThemedText type="smallBold" style={styles.previewButtonText}>
                  Primary button preview
                </ThemedText>
              </ThemedView>
            </SettingsCard>
          </ThemedView>
        </ThemedView>
        {Platform.OS === 'web' && <WebBadge />}
      </ThemedView>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scrollView: {
    flex: 1,
  },
  contentContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
  container: {
    maxWidth: MaxContentWidth,
    flexGrow: 1,
  },
  titleContainer: {
    gap: Spacing.three,
    alignItems: 'center',
    paddingHorizontal: Spacing.four,
    paddingVertical: Spacing.six,
  },
  centerText: {
    textAlign: 'center',
  },
  sectionsWrapper: {
    gap: Spacing.five,
    paddingHorizontal: Spacing.four,
    paddingTop: Spacing.three,
  },
  identityCard: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.three,
  },
  appIcon: {
    width: 48,
    height: 48,
    borderRadius: Radius.card / 2,
  },
  identityText: {
    flex: 1,
    gap: Spacing.half,
  },
  section: {
    gap: Spacing.two,
  },
  sectionTitle: {
    marginLeft: Spacing.two,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  accentCard: {
    gap: Spacing.four,
  },
  swatchRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.three,
  },
  pressed: {
    opacity: 0.7,
  },
  swatch: {
    width: 40,
    height: 40,
    borderRadius: Radius.bubble,
    borderWidth: 2,
    borderColor: 'transparent',
    alignItems: 'center',
    justifyContent: 'center',
  },
  previewButton: {
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: Radius.card / 2,
    paddingVertical: Spacing.three,
  },
  previewButtonText: {
    color: '#ffffff',
  },
});
