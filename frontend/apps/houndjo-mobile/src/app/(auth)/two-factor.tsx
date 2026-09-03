import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useVerifyLoginChallenge } from '@api-client';

import { AuthScreen } from '@/components/auth-screen';
import { FormError } from '@/components/form-error';
import { SubmitButton } from '@/components/submit-button';
import { InputOTP } from '@/components/ui/input-otp';
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

  async function onSubmit(submittedCode: string = code) {
    setCodeError(undefined);
    setFormError(null);

    // The challenge lives on the server and is short-lived; without an id
    // there is nothing to verify against, so send the user back to re-auth.
    if (!challengeId) {
      showToast(t('auth.toasts.twoFactorExpired'), 'error');
      router.replace('/sign-in');
      return;
    }

    if (submittedCode.length !== CODE_LENGTH) {
      setCodeError(t('auth.twoFactor.codeLength', { count: CODE_LENGTH }));
      return;
    }

    try {
      const tokens = await mutateAsync({ data: { challengeId, code: submittedCode } });

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
      <InputOTP
        length={CODE_LENGTH}
        value={code}
        onChangeText={setCode}
        onComplete={(value) => void onSubmit(value)}
        error={codeError}
        disabled={isPending}
      />

      <FormError message={formError} />

      <SubmitButton
        label={t('auth.twoFactor.submit')}
        onPress={() => void onSubmit()}
        isPending={isPending}
      />
    </AuthScreen>
  );
}
