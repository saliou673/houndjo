import { ActivityIndicator, Pressable, StyleSheet } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

type SubmitButtonProps = {
  label: string;
  onPress: () => void;
  isPending?: boolean;
};

export function SubmitButton({ label, onPress, isPending = false }: SubmitButtonProps) {
  const theme = useTheme();

  return (
    <Pressable
      accessibilityRole="button"
      disabled={isPending}
      onPress={onPress}
      style={({ pressed }) => [
        styles.submit,
        { backgroundColor: theme.text },
        (pressed || isPending) && styles.pressed,
      ]}>
      {isPending ? (
        <ActivityIndicator color={theme.background} />
      ) : (
        <ThemedText type="smallBold" style={{ color: theme.background }}>
          {label}
        </ThemedText>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  submit: {
    marginTop: Spacing.two,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: Spacing.two,
    paddingVertical: Spacing.three,
    minHeight: 48,
  },
  pressed: {
    opacity: 0.7,
  },
});
