import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import { useDeleteCurrentAccount, useGetUserDetails } from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { useAuth } from '@/hooks/use-auth';

export default function DeleteAccountScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { signOut } = useAuth();
  const { data: user } = useGetUserDetails();

  const CONFIRMATION_WORD = t('settings.account.deleteAccount.confirmationWord');

  const [confirmationText, setConfirmationText] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useDeleteCurrentAccount({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  const isConfirmed = confirmationText.trim().toUpperCase() === CONFIRMATION_WORD;

  async function onSubmit() {
    setFormError(null);

    if (!isConfirmed) {
      setFormError(t('settings.account.deleteAccount.confirmationRequired', { word: CONFIRMATION_WORD }));
      return;
    }

    try {
      await mutateAsync({});
      queryClient.clear();
      await signOut();
      showToast(t('settings.account.deleteAccount.toastDeleted'), 'success');
      router.replace('/sign-in');
    } catch {
      setFormError(t('errors.generic'));
    }
  }

  return (
    <>
      <Stack.Screen options={{ title: t('settings.account.delete') }} />
      <SettingsListScreen>
        <SettingsCard>
          <ThemedText themeColor="danger" type="smallBold">
            {t('settings.account.deleteAccount.warningTitle')}
          </ThemedText>
          <ThemedText type="small" themeColor="textSecondary">
            {t('settings.account.deleteAccount.warningBody')}
          </ThemedText>

          {user && (
            <ThemedText type="small" themeColor="textSecondary">
              {t('settings.account.deleteAccount.accountLabel', { email: user.email })}
            </ThemedText>
          )}

          <FormTextField
            label={t('settings.account.deleteAccount.confirmationLabel', {
              word: CONFIRMATION_WORD,
            })}
            value={confirmationText}
            onChangeText={setConfirmationText}
            placeholder={CONFIRMATION_WORD}
            autoCapitalize="characters"
            autoCorrect={false}
            editable={!isPending}
          />

          {formError && (
            <ThemedText type="small" themeColor="danger">
              {formError}
            </ThemedText>
          )}

          <SubmitButton
            label={t('settings.account.deleteAccount.submit')}
            onPress={() => void onSubmit()}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
