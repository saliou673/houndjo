import { Pressable, StyleSheet } from 'react-native';
import type { Href } from 'expo-router';
import { useRouter } from 'expo-router';
import { SymbolView, type SymbolViewProps } from 'expo-symbols';

import { ThemedText } from '@/components/themed-text';
import { View } from '@/components/ui/view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

type SettingsRowProps = {
  // Typed as `string` rather than `Href`: rows in this stacked epic are wired
  // up ahead of the screen they point to being built by a later issue, so
  // Expo Router's generated route union doesn't include the target yet.
  href: string;
  title: string;
  subtitle?: string;
  destructive?: boolean;
  icon?: SymbolViewProps['name'];
};

export function SettingsRow({ href, title, subtitle, destructive = false, icon }: SettingsRowProps) {
  const router = useRouter();
  const theme = useTheme();
  const iconColor = destructive ? theme.danger : theme.textSecondary;

  return (
    <Pressable
      accessibilityRole="button"
      onPress={() => router.push(href as Href)}
      style={({ pressed }) => [styles.row, pressed && styles.pressed]}>
      {icon && <SymbolView name={icon} size={20} weight="regular" tintColor={iconColor} />}

      <View style={styles.textColumn}>
        <ThemedText themeColor={destructive ? 'danger' : 'text'}>{title}</ThemedText>
        {subtitle && (
          <ThemedText type="small" themeColor="textSecondary">
            {subtitle}
          </ThemedText>
        )}
      </View>

      <SymbolView
        name={{ ios: 'chevron.right', android: 'chevron_right', web: 'chevron_right' }}
        size={14}
        weight="semibold"
        tintColor={theme.textSecondary}
      />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: Spacing.three,
    paddingHorizontal: Spacing.four,
    gap: Spacing.three,
  },
  textColumn: {
    flex: 1,
    gap: Spacing.half,
  },
  pressed: {
    opacity: 0.6,
  },
});
