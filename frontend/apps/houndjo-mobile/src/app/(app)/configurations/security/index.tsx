import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack } from 'expo-router';
import {
  getSecuritySettingsAsAdminQueryKey,
  useGetSecuritySettingsAsAdmin,
  useUpsertSecuritySettingsAsAdmin,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { ThemedText } from '@/components/themed-text';
import { Spinner } from '@/components/ui/spinner';
import { Switch } from '@/components/ui/switch';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';

export default function SecuritySettingsScreen() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { data: settings, isLoading, isError } = useGetSecuritySettingsAsAdmin();

  const { mutate: upsertSettings, isPending } = useUpsertSecuritySettingsAsAdmin({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function onToggle(twoFactorRequired: boolean) {
    upsertSettings(
      { data: { twoFactorRequired } },
      {
        onError: () => showToast(t('configurations.security.saveError'), 'error'),
        onSuccess: () => {
          void queryClient.invalidateQueries({ queryKey: getSecuritySettingsAsAdminQueryKey() });
          showToast(t('configurations.security.toastUpdated'), 'success');
        },
      }
    );
  }

  return (
    <>
      <Stack.Screen options={{ title: t('configurations.security.title') }} />
      <SettingsListScreen description={t('configurations.security.description')}>
        {isLoading ? (
          <Spinner />
        ) : isError || !settings ? (
          <ThemedText themeColor="danger">{t('configurations.security.loadError')}</ThemedText>
        ) : (
          <SettingsCard style={styles.card}>
            <ThemedText type="smallBold">{t('configurations.security.twoFactorSectionTitle')}</ThemedText>

            <View style={styles.switchRow}>
              <View style={styles.switchLabel}>
                <ThemedText>{t('configurations.security.twoFactorRequiredLabel')}</ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  {t('configurations.security.twoFactorRequiredDescription')}
                </ThemedText>
              </View>
              <Switch
                value={settings.twoFactorRequired ?? false}
                disabled={isPending}
                onValueChange={onToggle}
              />
            </View>
          </SettingsCard>
        )}
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: Spacing.three,
  },
  switchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.three,
  },
  switchLabel: {
    flex: 1,
    gap: Spacing.half,
  },
});
