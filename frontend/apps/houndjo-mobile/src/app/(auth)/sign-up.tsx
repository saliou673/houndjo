import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { Link, useRouter } from 'expo-router';
import { useCreatePublicUserAccount, type ValidationErrorResponseDTO } from '@api-client';

import { AuthScreen } from '@/components/auth-screen';
import { FormError } from '@/components/form-error';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD_LENGTH = 8;
// Mirrors the backend's CreateUserRequest password constraint.
const PASSWORD_PATTERN = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$/;

type FieldKey = 'firstName' | 'lastName' | 'email' | 'password' | 'confirmPassword';
type FieldErrors = Partial<Record<FieldKey, string>>;

export default function SignUpScreen() {
  const { t } = useTranslation();
  const router = useRouter();

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useCreatePublicUserAccount({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function validate(): FieldErrors {
    const errors: FieldErrors = {};

    if (!firstName) errors.firstName = t('auth.signUp.firstNameRequired');
    if (!lastName) errors.lastName = t('auth.signUp.lastNameRequired');

    if (!email) {
      errors.email = t('auth.signUp.emailRequired');
    } else if (!EMAIL_PATTERN.test(email)) {
      errors.email = t('auth.signUp.emailInvalid');
    }

    if (!password) {
      errors.password = t('auth.signUp.passwordRequired');
    } else if (password.length < MIN_PASSWORD_LENGTH) {
      errors.password = t('auth.signUp.passwordTooShort');
    } else if (!PASSWORD_PATTERN.test(password)) {
      errors.password = t('auth.signUp.passwordTooWeak');
    }

    if (!confirmPassword) {
      errors.confirmPassword = t('auth.signUp.confirmPasswordRequired');
    } else if (confirmPassword !== password) {
      errors.confirmPassword = t('auth.signUp.passwordsDoNotMatch');
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
      setFieldErrors({
        firstName: data.errors.firstName,
        lastName: data.errors.lastName,
        email: data.errors.email,
        password: data.errors.password,
      });

      if (Object.values(data.errors).some(Boolean)) return;
    }

    setFormError(extractApiErrorMessage(error, t('errors.generic')));
  }

  async function onSubmit() {
    const errors = validate();
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      await mutateAsync({ data: { firstName, lastName, email, password } });
      // Registration isn't finished until the emailed activation code is
      // entered (#15), so hand the user straight to that step.
      showToast(t('auth.toasts.accountCreated'), 'success');
      router.replace({ pathname: '/activate', params: { email } });
    } catch (error) {
      applyApiError(error);
    }
  }

  return (
    <AuthScreen title={t('auth.signUp.title')} subtitle={t('auth.signUp.subtitle')}>
      <FormTextField
        label={t('auth.signUp.firstNameLabel')}
        value={firstName}
        onChangeText={setFirstName}
        placeholder={t('auth.signUp.firstNamePlaceholder')}
        error={fieldErrors.firstName}
        autoComplete="given-name"
        editable={!isPending}
      />

      <FormTextField
        label={t('auth.signUp.lastNameLabel')}
        value={lastName}
        onChangeText={setLastName}
        placeholder={t('auth.signUp.lastNamePlaceholder')}
        error={fieldErrors.lastName}
        autoComplete="family-name"
        editable={!isPending}
      />

      <FormTextField
        label={t('auth.signUp.emailLabel')}
        value={email}
        onChangeText={setEmail}
        placeholder={t('auth.signUp.emailPlaceholder')}
        error={fieldErrors.email}
        autoCapitalize="none"
        autoComplete="email"
        autoCorrect={false}
        keyboardType="email-address"
        editable={!isPending}
      />

      <FormTextField
        label={t('auth.signUp.passwordLabel')}
        value={password}
        onChangeText={setPassword}
        placeholder={t('auth.signUp.passwordPlaceholder')}
        error={fieldErrors.password}
        autoCapitalize="none"
        autoComplete="new-password"
        autoCorrect={false}
        secureTextEntry
        editable={!isPending}
      />

      <FormTextField
        label={t('auth.signUp.confirmPasswordLabel')}
        value={confirmPassword}
        onChangeText={setConfirmPassword}
        placeholder={t('auth.signUp.passwordPlaceholder')}
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
        label={t('auth.signUp.submit')}
        onPress={() => void onSubmit()}
        isPending={isPending}
      />

      <View style={styles.footer}>
        <ThemedText type="small" themeColor="textSecondary">
          {t('auth.signUp.haveAccount')}
        </ThemedText>
        <Link href="/sign-in" replace>
          <ThemedText type="linkPrimary">{t('auth.signUp.signInLink')}</ThemedText>
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
