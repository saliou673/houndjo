import { Stack } from 'expo-router';
import { useTranslation } from 'react-i18next';

import { SettingsListScreen } from '@/components/settings-list-screen';
import { SettingsRow } from '@/components/settings-row';
import { SettingsSection } from '@/components/settings-section';

export default function SettingsAccountScreen() {
  const { t } = useTranslation();

  return (
    <>
      <Stack.Screen options={{ title: t('settings.account.title') }} />
      <SettingsListScreen description={t('settings.account.description')}>
        <SettingsSection title={t('settings.account.sectionSecurity')}>
          <SettingsRow
            href="/settings/account/email"
            title={t('settings.account.email')}
            icon={{ ios: 'envelope', android: 'mail', web: 'mail' }}
          />
          <SettingsRow
            href="/settings/account/password"
            title={t('settings.account.password')}
            icon={{ ios: 'key', android: 'password', web: 'password' }}
          />
          <SettingsRow
            href="/settings/account/two-factor"
            title={t('settings.account.twoFactor')}
            icon={{ ios: 'shield.checkered', android: 'verified_user', web: 'verified_user' }}
          />
        </SettingsSection>

        <SettingsSection title={t('settings.account.sectionDanger')}>
          <SettingsRow
            href="/settings/account/delete"
            title={t('settings.account.delete')}
            icon={{ ios: 'trash', android: 'delete', web: 'delete' }}
            destructive
          />
        </SettingsSection>
      </SettingsListScreen>
    </>
  );
}
