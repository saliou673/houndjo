import { Pressable, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

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
  const theme = useTheme();

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
              {actions.map((action) => {
                const isPrimary = action.variant !== 'outline';
                return (
                  <Pressable
                    key={action.label}
                    accessibilityRole="button"
                    onPress={action.onPress}
                    style={({ pressed }) => [
                      styles.button,
                      isPrimary
                        ? { backgroundColor: theme.text }
                        : { borderWidth: 1, borderColor: theme.backgroundSelected },
                      pressed && styles.pressed,
                    ]}>
                    <ThemedText
                      type="smallBold"
                      style={isPrimary ? { color: theme.background } : undefined}>
                      {action.label}
                    </ThemedText>
                  </Pressable>
                );
              })}
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
  button: {
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.four,
    paddingVertical: Spacing.two,
    minHeight: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressed: {
    opacity: 0.7,
  },
});
