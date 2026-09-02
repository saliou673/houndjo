import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useVerifyLoginChallenge } from '@api-client';

import { AuthScreen } from '@/components/auth-screen';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { useAuth } from '@/hooks/use-auth';
import { extractApiErrorMessage } from '@/lib/api-error';

const CODE_LENGTH = 6;

export default function TwoFactorScreen() {
  const { signIn } = useAuth();
  const { t } = useTranslation();
  const router = useRouter();
  const { challengeId } = useLocalSearchParams<{ challengeId?: string }>();

  const [code, setCode] = useState('');
  const [codeError, setCodeError] = useState<string | undefined>();
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useVerifyLoginChallenge({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  async function onSubmit() {
    setCodeError(undefined);
    setFormError(null);

    // The challenge lives on the server and is short-lived; without an id
    // there is nothing to verify against, so send the user back to re-auth.
    if (!challengeId) {
      showToast(t('auth.toasts.twoFactorExpired'), 'error');
      router.replace('/sign-in');
      return;
    }

    if (code.length !== CODE_LENGTH) {
      setCodeError(t('auth.twoFactor.codeLength', { count: CODE_LENGTH }));
      return;
    }

    try {
      const tokens = await mutateAsync({ data: { challengeId, code } });

      if (!tokens.accessToken || !tokens.refreshToken) {
        setFormError(t('errors.generic'));
        return;
      }

      await signIn({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken });
      showToast(t('auth.toasts.twoFactorVerified'), 'success');
      router.replace('/');
    } catch (error) {
      if (error instanceof AxiosError) {
        setCodeError(extractApiErrorMessage(error, t('auth.twoFactor.invalidCode')));
        return;
      }

      setFormError(t('errors.generic'));
    }
  }

  return (
    <AuthScreen title={t('auth.twoFactor.title')} subtitle={t('auth.twoFactor.subtitle')}>
      <FormTextField
        label={t('auth.twoFactor.codeLabel')}
        value={code}
        onChangeText={(value) => setCode(value.replace(/\D/g, '').slice(0, CODE_LENGTH))}
        placeholder={t('auth.twoFactor.codePlaceholder')}
        error={codeError}
        autoComplete="one-time-code"
        autoCorrect={false}
        keyboardType="number-pad"
        maxLength={CODE_LENGTH}
        editable={!isPending}
        onSubmitEditing={() => void onSubmit()}
      />

      {formError && (
        <ThemedText type="small" themeColor="danger">
          {formError}
        </ThemedText>
      )}

      <SubmitButton
        label={t('auth.twoFactor.submit')}
        onPress={() => void onSubmit()}
        isPending={isPending}
      />
    </AuthScreen>
  );
}
