import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { Stack, useRouter } from 'expo-router';
import { useChangePassword } from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { extractApiErrorMessage } from '@/lib/api-error';

const MIN_PASSWORD_LENGTH = 8;
// Mirrors the backend's PasswordChangeRequest constraint.
const PASSWORD_PATTERN = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$/;

type FieldErrors = {
  currentPassword?: string;
  newPassword?: string;
  confirmPassword?: string;
};

export default function ChangePasswordScreen() {
  const { t } = useTranslation();
  const router = useRouter();

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useChangePassword({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function validate(): FieldErrors {
    const errors: FieldErrors = {};

    if (!currentPassword) {
      errors.currentPassword = t('settings.account.changePassword.currentPasswordRequired');
    }

    if (!newPassword) {
      errors.newPassword = t('settings.account.changePassword.newPasswordRequired');
    } else if (newPassword.length < MIN_PASSWORD_LENGTH) {
      errors.newPassword = t('settings.account.changePassword.newPasswordTooShort');
    } else if (!PASSWORD_PATTERN.test(newPassword)) {
      errors.newPassword = t('settings.account.changePassword.newPasswordTooWeak');
    }

    if (!confirmPassword) {
      errors.confirmPassword = t('settings.account.changePassword.confirmPasswordRequired');
    } else if (confirmPassword !== newPassword) {
      errors.confirmPassword = t('settings.account.changePassword.passwordsDoNotMatch');
    }

    return errors;
  }

  async function onSubmit() {
    const errors = validate();
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      await mutateAsync({ data: { currentPassword, newPassword } });
      showToast(t('settings.account.changePassword.toastUpdated'), 'success');
      router.back();
    } catch (error) {
      if (error instanceof AxiosError) {
        const status = error.response?.status;

        if (status === 403 || status === 409) {
          setFieldErrors({
            currentPassword: extractApiErrorMessage(
              error,
              t('settings.account.changePassword.invalidCurrentPassword')
            ),
          });
          return;
        }

        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }

      setFormError(t('errors.generic'));
    }
  }

  return (
    <>
      <Stack.Screen options={{ title: t('settings.account.password') }} />
      <SettingsListScreen description={t('settings.account.changePassword.description')}>
        <SettingsCard>
          <FormTextField
            label={t('settings.account.changePassword.currentPasswordLabel')}
            value={currentPassword}
            onChangeText={setCurrentPassword}
            error={fieldErrors.currentPassword}
            autoCapitalize="none"
            autoComplete="current-password"
            autoCorrect={false}
            secureTextEntry
            editable={!isPending}
          />

          <FormTextField
            label={t('settings.account.changePassword.newPasswordLabel')}
            value={newPassword}
            onChangeText={setNewPassword}
            error={fieldErrors.newPassword}
            autoCapitalize="none"
            autoComplete="new-password"
            autoCorrect={false}
            secureTextEntry
            editable={!isPending}
          />

          <FormTextField
            label={t('settings.account.changePassword.confirmPasswordLabel')}
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            error={fieldErrors.confirmPassword}
            autoCapitalize="none"
            autoComplete="new-password"
            autoCorrect={false}
            secureTextEntry
            editable={!isPending}
            onSubmitEditing={() => void onSubmit()}
          />

          {formError && (
            <ThemedText type="small" themeColor="danger">
              {formError}
            </ThemedText>
          )}

          <SubmitButton
            label={t('settings.account.changePassword.submit')}
            onPress={() => void onSubmit()}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
