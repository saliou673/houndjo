import { StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/button';
import { View } from '@/components/ui/view';
import { Spacing } from '@/constants/theme';

export type ErrorScreenAction = {
  label: string;
  onPress: () => void;
  variant?: 'primary' | 'outline';
};

type ErrorScreenProps = {
  code?: string;
  title: string;
  description: string;
  actions?: ErrorScreenAction[];
};

export function ErrorScreen({ code, title, description, actions }: ErrorScreenProps) {
  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.container} edges={['bottom']}>
        <View style={styles.content}>
          {code && (
            <ThemedText type="title" style={styles.code}>
              {code}
            </ThemedText>
          )}
          <ThemedText type="smallBold" style={styles.title}>
            {title}
          </ThemedText>
          <ThemedText type="small" themeColor="textSecondary" style={styles.description}>
            {description}
          </ThemedText>

          {actions && actions.length > 0 && (
            <View style={styles.actions}>
              {actions.map((action) => (
                <Button
                  key={action.label}
                  label={action.label}
                  onPress={action.onPress}
                  variant={action.variant === 'outline' ? 'outline' : 'default'}>
                  {action.label}
                </Button>
              ))}
            </View>
          )}
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.two,
    paddingHorizontal: Spacing.four,
  },
  code: {
    fontSize: 64,
  },
  title: {
    fontSize: 18,
  },
  description: {
    textAlign: 'center',
  },
  actions: {
    flexDirection: 'row',
    gap: Spacing.three,
    marginTop: Spacing.four,
  },
});
