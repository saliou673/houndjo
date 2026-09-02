import { Stack } from 'expo-router';
import { useTranslation } from 'react-i18next';

import { SettingsListScreen } from '@/components/settings-list-screen';
import { SettingsRow } from '@/components/settings-row';
import { SettingsSection } from '@/components/settings-section';

export default function ConfigurationsHomeScreen() {
  const { t } = useTranslation();

  return (
    <>
      <Stack.Screen options={{ title: t('configurations.nav.title') }} />
      <SettingsListScreen description={t('configurations.nav.description')}>
        <SettingsSection>
          <SettingsRow
            href="/configurations/reference-data"
            title={t('configurations.nav.referenceData')}
            icon={{ ios: 'tag', android: 'sell', web: 'sell' }}
          />
          <SettingsRow
            href="/configurations/file-storage"
            title={t('configurations.nav.fileStorage')}
            icon={{ ios: 'externaldrive', android: 'storage', web: 'storage' }}
          />
          <SettingsRow
            href="/configurations/tax-rates"
            title={t('configurations.nav.taxRates')}
            icon={{ ios: 'percent', android: 'percent', web: 'percent' }}
          />
          <SettingsRow
            href="/configurations/company-profile"
            title={t('configurations.nav.companyProfile')}
            icon={{ ios: 'building.2', android: 'business', web: 'business' }}
          />
          <SettingsRow
            href="/configurations/security"
            title={t('configurations.nav.security')}
            icon={{ ios: 'exclamationmark.shield', android: 'gpp_maybe', web: 'gpp_maybe' }}
          />
        </SettingsSection>
      </SettingsListScreen>
    </>
  );
}
