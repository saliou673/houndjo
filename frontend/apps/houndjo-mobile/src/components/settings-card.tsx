import { StyleSheet, type ViewProps } from 'react-native';

import { Card } from '@/components/ui/card';
import { Spacing } from '@/constants/theme';

export function SettingsCard({ style, children, ...props }: ViewProps) {
  return (
    <Card style={StyleSheet.flatten([styles.card, style])} {...props}>
      {children}
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: Spacing.three,
  },
});
