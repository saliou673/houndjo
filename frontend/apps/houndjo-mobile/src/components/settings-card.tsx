import { StyleSheet } from 'react-native';

import { ThemedView, type ThemedViewProps } from '@/components/themed-view';
import { CardShadow, Radius, Spacing } from '@/constants/theme';

export function SettingsCard({ style, ...props }: ThemedViewProps) {
  return <ThemedView type="backgroundElement" style={[styles.card, style]} {...props} />;
}

const styles = StyleSheet.create({
  card: {
    borderRadius: Radius.card,
    padding: Spacing.three,
    gap: Spacing.three,
    ...CardShadow,
  },
});
