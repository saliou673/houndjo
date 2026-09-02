import { useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'expo-router';
import { SymbolView } from 'expo-symbols';
import { useLogout } from '@api-client';

import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { useAuth } from '@/hooks/use-auth';
import { useTheme } from '@/hooks/use-theme';

export function LogoutButton() {
  const { t } = useTranslation();
  const theme = useTheme();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { signOut } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const { mutateAsync } = useLogout({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  async function onLogout() {
    setIsLoggingOut(true);

    try {
      // Revoke server-side first, while the access token is still attached.
      // A failure here (offline, already-expired token) must not strand the
      // user in a session they asked to end, so the local teardown runs either
      // way.
      await mutateAsync({});
    } catch {
      // Intentionally ignored - see above.
    }

    await signOut();
    queryClient.clear();
    showToast(t('auth.toasts.signedOut'), 'success');
    router.replace('/sign-in');
  }

  return (
    <Pressable
      accessibilityRole="button"
      disabled={isLoggingOut}
      onPress={() => void onLogout()}
      style={({ pressed }) => [styles.row, pressed && styles.pressed]}>
      <SymbolView
        name={{ ios: 'rectangle.portrait.and.arrow.right', android: 'logout', web: 'logout' }}
        size={20}
        weight="regular"
        tintColor={theme.danger}
      />

      <View style={styles.textColumn}>
        <ThemedText themeColor="danger">{t('auth.logout.action')}</ThemedText>
      </View>

      {isLoggingOut && <ActivityIndicator color={theme.danger} />}
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
  },
  pressed: {
    opacity: 0.6,
  },
});
