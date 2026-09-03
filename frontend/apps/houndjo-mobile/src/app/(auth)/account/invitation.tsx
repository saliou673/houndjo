import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { Link, useLocalSearchParams, useRouter } from 'expo-router';
import { useCompleteInvitation } from '@api-client';

import { AuthScreen } from '@/components/auth-screen';
import { FormError } from '@/components/form-error';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { extractApiErrorMessage } from '@/lib/api-error';

const MIN_PASSWORD_LENGTH = 8;
// Mirrors the backend's InvitationCompleteRequest constraint.
const PASSWORD_PATTERN = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$/;

type FieldErrors = {
  newPassword?: string;
  confirmPassword?: string;
};

export default function InvitationScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { code } = useLocalSearchParams<{ code?: string }>();

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useCompleteInvitation({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function validate(): FieldErrors {
    const errors: FieldErrors = {};

    if (!newPassword) {
      errors.newPassword = t('auth.invitation.passwordRequired');
    } else if (newPassword.length < MIN_PASSWORD_LENGTH) {
      errors.newPassword = t('auth.invitation.passwordTooShort');
    } else if (!PASSWORD_PATTERN.test(newPassword)) {
      errors.newPassword = t('auth.invitation.passwordTooWeak');
    }

    if (!confirmPassword) {
      errors.confirmPassword = t('auth.invitation.confirmPasswordRequired');
    } else if (confirmPassword !== newPassword) {
      errors.confirmPassword = t('auth.invitation.passwordsDoNotMatch');
    }

    return errors;
  }

  async function onSubmit() {
    if (!code) return;

    const errors = validate();
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      await mutateAsync({ data: { code, newPassword } });
      showToast(t('auth.toasts.invitationCompleted'), 'success');
      router.replace('/sign-in');
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('auth.invitation.invalidCode')));
        return;
      }

      setFormError(t('errors.generic'));
    }
  }

  // The code only ever arrives from the invitation link, so there is no field
  // to fall back to - without it the screen has nothing to complete.
  if (!code) {
    return (
      <AuthScreen title={t('auth.invitation.title')}>
        <FormError message={t('auth.invitation.missingCode')} />
        <Link href="/sign-in" replace>
          <ThemedText type="linkPrimary">{t('auth.invitation.backToSignIn')}</ThemedText>
        </Link>
      </AuthScreen>
    );
  }

  return (
    <AuthScreen title={t('auth.invitation.title')} subtitle={t('auth.invitation.subtitle')}>
      <FormTextField
        label={t('auth.invitation.newPasswordLabel')}
        value={newPassword}
        onChangeText={setNewPassword}
        placeholder={t('auth.invitation.passwordPlaceholder')}
        error={fieldErrors.newPassword}
        autoCapitalize="none"
        autoComplete="new-password"
        autoCorrect={false}
        secureTextEntry
        editable={!isPending}
      />

      <FormTextField
        label={t('auth.invitation.confirmPasswordLabel')}
        value={confirmPassword}
        onChangeText={setConfirmPassword}
        placeholder={t('auth.invitation.passwordPlaceholder')}
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
        label={t('auth.invitation.submit')}
        onPress={() => void onSubmit()}
        isPending={isPending}
      />
    </AuthScreen>
  );
}
