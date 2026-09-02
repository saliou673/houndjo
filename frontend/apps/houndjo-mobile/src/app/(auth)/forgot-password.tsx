import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { Link } from 'expo-router';
import { useRequestPasswordReset } from '@api-client';

import { AuthScreen } from '@/components/auth-screen';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function ForgotPasswordScreen() {
  const { t } = useTranslation();

  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState<string | undefined>();
  const [formError, setFormError] = useState<string | null>(null);
  const [isSent, setIsSent] = useState(false);

  const { mutateAsync, isPending } = useRequestPasswordReset({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  async function onSubmit() {
    setEmailError(undefined);
    setFormError(null);

    if (!email) {
      setEmailError(t('auth.forgotPassword.emailRequired'));
      return;
    }

    if (!EMAIL_PATTERN.test(email)) {
      setEmailError(t('auth.forgotPassword.emailInvalid'));
      return;
    }

    try {
      await mutateAsync({ data: email });
      showToast(t('auth.toasts.resetCodeSent', { email }), 'success');
      setIsSent(true);
    } catch (error) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 404) {
          setEmailError(t('auth.forgotPassword.noAccount'));
          return;
        }

        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }

      setFormError(t('errors.generic'));
    }
  }

  if (isSent) {
    return (
      <AuthScreen
        title={t('auth.forgotPassword.sentTitle')}
        subtitle={t('auth.forgotPassword.sentSubtitle', { email })}>
        <Link href="/reset-password" replace>
          <ThemedText type="linkPrimary">{t('auth.forgotPassword.enterCodeLink')}</ThemedText>
        </Link>
        <Link href="/sign-in" replace>
          <ThemedText type="linkPrimary">{t('auth.forgotPassword.backToSignIn')}</ThemedText>
        </Link>
      </AuthScreen>
    );
  }

  return (
    <AuthScreen
      title={t('auth.forgotPassword.title')}
      subtitle={t('auth.forgotPassword.subtitle')}>
      <FormTextField
        label={t('auth.forgotPassword.emailLabel')}
        value={email}
        onChangeText={setEmail}
        placeholder={t('auth.forgotPassword.emailPlaceholder')}
        error={emailError}
        autoCapitalize="none"
        autoComplete="email"
        autoCorrect={false}
        keyboardType="email-address"
        editable={!isPending}
        onSubmitEditing={() => void onSubmit()}
      />

      {formError && (
        <ThemedText type="small" themeColor="danger">
          {formError}
        </ThemedText>
      )}

      <SubmitButton
        label={t('auth.forgotPassword.submit')}
        onPress={() => void onSubmit()}
        isPending={isPending}
      />

      <View style={styles.footer}>
        <Link href="/sign-in" replace>
          <ThemedText type="linkPrimary">{t('auth.forgotPassword.backToSignIn')}</ThemedText>
        </Link>
      </View>
    </AuthScreen>
  );
}

const styles = StyleSheet.create({
  footer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.two,
  },
});
