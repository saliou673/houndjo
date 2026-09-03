import { Children, Fragment, type ReactNode } from 'react';
import { StyleSheet, View } from 'react-native';

import { SettingsCard } from '@/components/settings-card';
import { ThemedText } from '@/components/themed-text';
import { Separator } from '@/components/ui/separator';
import { Spacing } from '@/constants/theme';

type SettingsSectionProps = {
  title?: string;
  children: ReactNode;
};

export function SettingsSection({ title, children }: SettingsSectionProps) {
  const rows = Children.toArray(children);

  return (
    <View style={styles.section}>
      {title && (
        <ThemedText type="smallBold" themeColor="textSecondary" style={styles.title}>
          {title}
        </ThemedText>
      )}
      <SettingsCard style={styles.card}>
        {rows.map((row, index) => (
          <Fragment key={index}>
            {row}
            {index < rows.length - 1 && <Separator style={styles.divider} />}
          </Fragment>
        ))}
      </SettingsCard>
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    gap: Spacing.two,
  },
  title: {
    marginLeft: Spacing.two,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  card: {
    padding: 0,
    gap: 0,
  },
  divider: {
    height: StyleSheet.hairlineWidth,
    marginLeft: Spacing.four + 20 + Spacing.three,
  },
});
