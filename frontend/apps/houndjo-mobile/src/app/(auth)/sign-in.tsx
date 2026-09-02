import { useState } from 'react';
import { StyleSheet, Switch, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { Link, useRouter } from 'expo-router';
import { useAuthenticate, type ValidationErrorResponseDTO } from '@api-client';

import { AuthScreen } from '@/components/auth-screen';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { useAuth } from '@/hooks/use-auth';
import { extractApiErrorMessage } from '@/lib/api-error';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD_LENGTH = 7;

type FieldErrors = {
  email?: string;
  password?: string;
};

/**
 * `POST /api/auth/login` answers 200 with a JwtToken or 202 with a 2FA
 * challenge, but the controller returns `ResponseEntity<?>` so the generated
 * response type erases to `object`. Both shapes have to be narrowed by hand
 * until the endpoint declares its schemas.
 */
type LoginTokens = { accessToken: string; refreshToken: string };

function getChallengeId(response: unknown): string | null {
  if (typeof response !== 'object' || response === null) return null;
  const candidate = response as { challengeId?: unknown };
  return typeof candidate.challengeId === 'string' ? candidate.challengeId : null;
}

function isLoginTokens(response: unknown): response is LoginTokens {
  if (typeof response !== 'object' || response === null) return false;
  const candidate = response as Partial<LoginTokens>;
  return typeof candidate.accessToken === 'string' && typeof candidate.refreshToken === 'string';
}

export default function SignInScreen() {
  const { signIn } = useAuth();
  const { t } = useTranslation();
  const router = useRouter();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useAuthenticate({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function validate(): FieldErrors {
    const errors: FieldErrors = {};

    if (!email) {
      errors.email = t('auth.signIn.emailRequired');
    } else if (!EMAIL_PATTERN.test(email)) {
      errors.email = t('auth.signIn.emailInvalid');
    }

    if (!password) {
      errors.password = t('auth.signIn.passwordRequired');
    } else if (password.length < MIN_PASSWORD_LENGTH) {
      errors.password = t('auth.signIn.passwordTooShort');
    }

    return errors;
  }

  function applyApiError(error: unknown) {
    if (!(error instanceof AxiosError)) {
      setFormError(t('errors.generic'));
      return;
    }

    const data = error.response?.data as ValidationErrorResponseDTO | undefined;

    if (data?.errors) {
      setFieldErrors({ email: data.errors.email, password: data.errors.password });
    }

    if (data?.errors?.email || data?.errors?.password) return;

    if (error.response?.status === 401) {
      setFormError(extractApiErrorMessage(error, t('auth.signIn.invalidCredentials')));
      return;
    }

    setFormError(extractApiErrorMessage(error, t('errors.generic')));
  }

  async function onSubmit() {
    const errors = validate();
    setFieldErrors(errors);
    setFormError(null);

    if (errors.email || errors.password) return;

    try {
      const response = await mutateAsync({ data: { email, password, rememberMe } });

      const challengeId = getChallengeId(response);
      if (challengeId) {
        router.push({ pathname: '/two-factor', params: { challengeId } });
        return;
      }

      if (!isLoginTokens(response)) {
        setFormError(t('errors.generic'));
        return;
      }

      await signIn({
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      });
      showToast(t('auth.toasts.signedIn'), 'success');
      router.replace('/');
    } catch (error) {
      applyApiError(error);
    }
  }

  return (
    <AuthScreen title={t('auth.signIn.title')} subtitle={t('auth.signIn.subtitle')}>
      <FormTextField
        label={t('auth.signIn.emailLabel')}
        value={email}
        onChangeText={setEmail}
        placeholder={t('auth.signIn.emailPlaceholder')}
        error={fieldErrors.email}
        autoCapitalize="none"
        autoComplete="email"
        autoCorrect={false}
        keyboardType="email-address"
        editable={!isPending}
      />

      <FormTextField
        label={t('auth.signIn.passwordLabel')}
        value={password}
        onChangeText={setPassword}
        placeholder={t('auth.signIn.passwordPlaceholder')}
        error={fieldErrors.password}
        autoCapitalize="none"
        autoComplete="current-password"
        autoCorrect={false}
        secureTextEntry
        editable={!isPending}
        onSubmitEditing={() => void onSubmit()}
      />

      <Link href="/forgot-password" style={styles.forgotPassword}>
        <ThemedText type="linkPrimary">{t('auth.signIn.forgotPassword')}</ThemedText>
      </Link>

      <View style={styles.rememberMeRow}>
        <ThemedText type="small">{t('auth.signIn.rememberMe')}</ThemedText>
        <Switch
          value={rememberMe}
          onValueChange={setRememberMe}
          disabled={isPending}
          accessibilityLabel={t('auth.signIn.rememberMe')}
        />
      </View>

      {formError && (
        <ThemedText type="small" themeColor="danger">
          {formError}
        </ThemedText>
      )}

      <SubmitButton
        label={t('auth.signIn.submit')}
        onPress={() => void onSubmit()}
        isPending={isPending}
      />

      <View style={styles.footer}>
        <ThemedText type="small" themeColor="textSecondary">
          {t('auth.signIn.noAccount')}
        </ThemedText>
        <Link href="/sign-up" replace>
          <ThemedText type="linkPrimary">{t('auth.signIn.signUpLink')}</ThemedText>
        </Link>
      </View>
    </AuthScreen>
  );
}

const styles = StyleSheet.create({
  forgotPassword: {
    alignSelf: 'flex-end',
  },
  rememberMeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  footer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.two,
  },
});
