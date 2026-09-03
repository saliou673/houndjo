import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { Link, useLocalSearchParams, useRouter } from 'expo-router';
import { useFinishPasswordReset } from '@api-client';

import { AuthScreen } from '@/components/auth-screen';
import { FormError } from '@/components/form-error';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { extractApiErrorMessage } from '@/lib/api-error';

const MIN_PASSWORD_LENGTH = 8;
// Mirrors the backend's PasswordResetRequest constraint.
const PASSWORD_PATTERN = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$/;

type FieldErrors = {
  code?: string;
  newPassword?: string;
  confirmPassword?: string;
};

export default function ResetPasswordScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const params = useLocalSearchParams<{ code?: string }>();

  const [code, setCode] = useState(params.code ?? '');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useFinishPasswordReset({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function validate(): FieldErrors {
    const errors: FieldErrors = {};

    if (!code) errors.code = t('auth.resetPassword.codeRequired');

    if (!newPassword) {
      errors.newPassword = t('auth.resetPassword.passwordRequired');
    } else if (newPassword.length < MIN_PASSWORD_LENGTH) {
      errors.newPassword = t('auth.resetPassword.passwordTooShort');
    } else if (!PASSWORD_PATTERN.test(newPassword)) {
      errors.newPassword = t('auth.resetPassword.passwordTooWeak');
    }

    if (!confirmPassword) {
      errors.confirmPassword = t('auth.resetPassword.confirmPasswordRequired');
    } else if (confirmPassword !== newPassword) {
      errors.confirmPassword = t('auth.resetPassword.passwordsDoNotMatch');
    }

    return errors;
  }

  async function onSubmit() {
    const errors = validate();
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      await mutateAsync({ data: { code, newPassword } });
      showToast(t('auth.toasts.passwordReset'), 'success');
      router.replace('/sign-in');
    } catch (error) {
      if (error instanceof AxiosError) {
        const status = error.response?.status;

        if (status === 403 || status === 404 || status === 409) {
          setFieldErrors({
            code: extractApiErrorMessage(error, t('auth.resetPassword.invalidCode')),
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
    <AuthScreen
      title={t('auth.resetPassword.title')}
      subtitle={t('auth.resetPassword.subtitle')}>
      <FormTextField
        label={t('auth.resetPassword.codeLabel')}
        value={code}
        onChangeText={setCode}
        placeholder={t('auth.resetPassword.codePlaceholder')}
        error={fieldErrors.code}
        autoCapitalize="none"
        autoCorrect={false}
        editable={!isPending}
      />

      <FormTextField
        label={t('auth.resetPassword.newPasswordLabel')}
        value={newPassword}
        onChangeText={setNewPassword}
        placeholder={t('auth.resetPassword.passwordPlaceholder')}
        error={fieldErrors.newPassword}
        autoCapitalize="none"
        autoComplete="new-password"
        autoCorrect={false}
        secureTextEntry
        editable={!isPending}
      />

      <FormTextField
        label={t('auth.resetPassword.confirmPasswordLabel')}
        value={confirmPassword}
        onChangeText={setConfirmPassword}
        placeholder={t('auth.resetPassword.passwordPlaceholder')}
        error={fieldErrors.confirmPassword}
        autoCapitalize="none"
        autoComplete="new-password"
        autoCorrect={false}
        secureTextEntry
        editable={!isPending}
        onSubmitEditing={() => void onSubmit()}
      />

      <FormError message={formError} />

      <SubmitButton
        label={t('auth.resetPassword.submit')}
        onPress={() => void onSubmit()}
        isPending={isPending}
      />

      <Link href="/sign-in" replace>
        <ThemedText type="linkPrimary">{t('auth.resetPassword.backToSignIn')}</ThemedText>
      </Link>
    </AuthScreen>
  );
}
