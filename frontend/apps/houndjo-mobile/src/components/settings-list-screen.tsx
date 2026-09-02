import type { ReactNode } from 'react';
import { ScrollView, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { View } from '@/components/ui/view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';

type SettingsListScreenProps = {
  description?: string;
  children: ReactNode;
};

export function SettingsListScreen({ description, children }: SettingsListScreenProps) {
  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.container} edges={['bottom']}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          contentInsetAdjustmentBehavior="automatic">
          <View style={styles.content}>
            {description && (
              <ThemedText type="small" themeColor="textSecondary">
                {description}
              </ThemedText>
            )}
            <View style={styles.list}>{children}</View>
          </View>
        </ScrollView>
      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    paddingHorizontal: Spacing.four,
    paddingTop: Spacing.four,
    paddingBottom: BottomTabInset + Spacing.four,
  },
  content: {
    width: '100%',
    maxWidth: MaxContentWidth,
    alignSelf: 'center',
    gap: Spacing.four,
  },
  list: {
    gap: Spacing.two,
  },
});
