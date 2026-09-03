import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Stack } from 'expo-router';
import { useGetUserDetails, useUpdateAccount } from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { Picker } from '@/components/ui/picker';
import { showToast } from '@/components/toast/toast-store';
import { setLanguage, SUPPORTED_LANGUAGES, type SupportedLanguage } from '@/i18n';

const LANGUAGE_LABEL_KEYS: Record<SupportedLanguage, string> = {
  en: 'languageSwitch.english',
  fr: 'languageSwitch.french',
};

export default function LanguageScreen() {
  const { t, i18n } = useTranslation();
  const { data: user } = useGetUserDetails();
  const [isSaving, setIsSaving] = useState(false);

  const { mutateAsync: updateAccount } = useUpdateAccount({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  const currentLanguage = i18n.language as SupportedLanguage;

  async function onSelect(next: SupportedLanguage) {
    if (next === currentLanguage || isSaving) return;

    setIsSaving(true);
    await setLanguage(next);

    if (user) {
      try {
        await updateAccount({
          data: {
            firstName: user.firstName,
            lastName: user.lastName,
            phoneNumber: user.phoneNumber,
            birthDate: user.birthDate,
            gender: user.gender,
            address: user.address,
            languageKey: next,
            imageUrl: user.imageUrl,
          },
        });
      } catch {
        // The device-local language already switched via setLanguage above -
        // only the account-level sync failed, so this is reported without
        // reverting the language the user just picked.
        showToast(t('settings.language.syncError'), 'error');
      }
    }

    setIsSaving(false);
  }

  return (
    <>
      <Stack.Screen options={{ title: t('settings.nav.language') }} />
      <SettingsListScreen description={t('settings.language.description')}>
        <SettingsCard>
          <Picker
            variant="outline"
            value={currentLanguage}
            onValueChange={(next) => void onSelect(next as SupportedLanguage)}
            disabled={isSaving}
            options={SUPPORTED_LANGUAGES.map((language) => ({
              value: language,
              label: t(LANGUAGE_LABEL_KEYS[language]),
            }))}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
