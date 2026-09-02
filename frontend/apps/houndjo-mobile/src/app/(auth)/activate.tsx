import { useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useMutation } from '@tanstack/react-query';
import { Link, useLocalSearchParams, useRouter } from 'expo-router';
import { activateAccount, useRequestActivationCode } from '@api-client';

import { AuthScreen } from '@/components/auth-screen';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Fonts, Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

// Mirrors `UserService.OTP_CODE_SIZE - 1`: 4 uppercase hex characters.
const CODE_LENGTH = 4;

/**
 * Registration is only complete once the OTP from the activation email is
 * entered, so sign-up lands here rather than on sign-in. The code may also
 * arrive prefilled from a `houndjomobile://activate?code=...` link.
 */
export default function ActivateScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { code: linkCode, email } = useLocalSearchParams<{ code?: string; email?: string }>();

  const [code, setCode] = useState(linkCode ?? '');
  const [codeError, setCodeError] = useState<string | undefined>();
  const [formError, setFormError] = useState<string | null>(null);

  // `useActivateAccount` is a query hook, but activation is a user-triggered
  // one-shot with side effects, so the client call is wrapped as a mutation.
  const { mutateAsync: activate, isPending } = useMutation({
    mutationFn: (activationCode: string) => activateAccount({ code: activationCode }),
    meta: { skipGlobalErrorToast: true },
  });

  const { mutateAsync: resendCode, isPending: isResending } = useRequestActivationCode({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function goToSignIn() {
    showToast(t('auth.toasts.accountActivated'), 'success');
    router.replace('/sign-in');
  }

  async function onSubmit() {
    setCodeError(undefined);
    setFormError(null);

    const activationCode = code.trim();

    if (!activationCode) {
      setCodeError(t('auth.activate.codeRequired'));
      return;
    }

    try {
      await activate(activationCode);
      goToSignIn();
    } catch (error) {
      if (error instanceof AxiosError) {
        // Already activated is a dead end rather than a failure - the account
        // is usable, so send the user on instead of asking for another code.
        if (error.response?.status === 409) {
          goToSignIn();
          return;
        }

        setCodeError(extractApiErrorMessage(error, t('auth.activate.invalidCode')));
        return;
      }

      setFormError(t('errors.generic'));
    }
  }

  async function onResend() {
    if (!email) return;

    setCodeError(undefined);
    setFormError(null);

    try {
      await resendCode({ data: email });
      showToast(t('auth.toasts.activationCodeSent'), 'success');
    } catch (error) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 409) {
          goToSignIn();
          return;
        }

        if (error.response?.status === 404) {
          setFormError(t('auth.activate.noAccount'));
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
      title={t('auth.activate.title')}
      subtitle={
        email ? t('auth.activate.subtitle', { email }) : t('auth.activate.subtitleGeneric')
      }>
      <FormTextField
        label={t('auth.activate.codeLabel')}
        value={code}
        onChangeText={(value) => setCode(value.toUpperCase())}
        placeholder={t('auth.activate.codePlaceholder')}
        error={codeError}
        autoCapitalize="characters"
        autoComplete="one-time-code"
        autoCorrect={false}
        maxLength={CODE_LENGTH}
        style={styles.codeInput}
        editable={!isPending}
        onSubmitEditing={() => void onSubmit()}
      />

      {formError && (
        <ThemedText type="small" themeColor="danger">
          {formError}
        </ThemedText>
      )}

      <SubmitButton
        label={t('auth.activate.submit')}
        onPress={() => void onSubmit()}
        isPending={isPending}
      />

      <View style={styles.footer}>
        {email && (
          <Pressable
            accessibilityRole="button"
            disabled={isResending}
            onPress={() => void onResend()}>
            <ThemedText type="linkPrimary">{t('auth.activate.resend')}</ThemedText>
          </Pressable>
        )}

        <Link href="/sign-in" replace>
          <ThemedText type="linkPrimary">{t('auth.activate.backToSignIn')}</ThemedText>
        </Link>
      </View>
    </AuthScreen>
  );
}

const styles = StyleSheet.create({
  codeInput: {
    fontFamily: Fonts.mono,
    fontSize: 28,
    // Trails the last character, so pad the left to keep the run centred.
    letterSpacing: 10,
    paddingLeft: Spacing.three + 10,
    paddingVertical: Spacing.three,
    textAlign: 'center',
  },
  footer: {
    alignItems: 'center',
    gap: Spacing.two,
  },
});
