import { useState } from 'react';
import { Pressable, StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import { getUserDetailsQueryKey, useConfirmEmailChange, useRequestEmailChange } from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Fonts, Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const CODE_LENGTH = 4;

export default function ChangeEmailScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [step, setStep] = useState<'request' | 'confirm'>('request');
  const [newEmail, setNewEmail] = useState('');
  const [emailError, setEmailError] = useState<string | undefined>();
  const [code, setCode] = useState('');
  const [codeError, setCodeError] = useState<string | undefined>();
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync: requestChange, isPending: isRequesting } = useRequestEmailChange({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });
  const { mutateAsync: confirmChange, isPending: isConfirming } = useConfirmEmailChange({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  async function onRequestSubmit() {
    setEmailError(undefined);
    setFormError(null);

    const trimmed = newEmail.trim();
    if (!EMAIL_PATTERN.test(trimmed)) {
      setEmailError(t('settings.account.emailChange.emailInvalid'));
      return;
    }

    try {
      await requestChange({ data: { newEmail: trimmed } });
      showToast(t('settings.account.emailChange.codeSentToast', { email: trimmed }), 'success');
      setStep('confirm');
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  async function onConfirmSubmit() {
    setCodeError(undefined);
    setFormError(null);

    const trimmedCode = code.trim();
    if (trimmedCode.length !== CODE_LENGTH) {
      setCodeError(t('settings.account.emailChange.codeLength', { count: CODE_LENGTH }));
      return;
    }

    try {
      await confirmChange({ data: { code: trimmedCode } });
      await queryClient.invalidateQueries({ queryKey: getUserDetailsQueryKey() });
      showToast(t('settings.account.emailChange.toastChanged'), 'success');
      // The user details refetch above can 401 and force a logout (e.g. a
      // stale session), which tears down this screen's navigator before we
      // get here - going back at that point has nothing to go back to.
      if (router.canGoBack()) {
        router.back();
      }
    } catch (error) {
      if (error instanceof AxiosError) {
        setCodeError(extractApiErrorMessage(error, t('settings.account.emailChange.invalidCode')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  function onCancelConfirm() {
    setStep('request');
    setCode('');
    setCodeError(undefined);
  }

  return (
    <>
      <Stack.Screen options={{ title: t('settings.account.email') }} />
      <SettingsListScreen
        description={
          step === 'request'
            ? t('settings.account.emailChange.description')
            : t('settings.account.emailChange.confirmDescription', { email: newEmail.trim() })
        }>
        <SettingsCard>
          {step === 'request' ? (
            <>
              <FormTextField
                label={t('settings.account.emailChange.newEmailLabel')}
                value={newEmail}
                onChangeText={setNewEmail}
                placeholder={t('settings.account.emailChange.newEmailPlaceholder')}
                error={emailError}
                keyboardType="email-address"
                autoCapitalize="none"
                autoCorrect={false}
                editable={!isRequesting}
                onSubmitEditing={() => void onRequestSubmit()}
              />

              {formError && (
                <ThemedText type="small" themeColor="danger">
                  {formError}
                </ThemedText>
              )}

              <SubmitButton
                label={t('settings.account.emailChange.sendButton')}
                onPress={() => void onRequestSubmit()}
                isPending={isRequesting}
              />
            </>
          ) : (
            <>
              <FormTextField
                label={t('settings.account.emailChange.codeLabel')}
                value={code}
                onChangeText={(text) => setCode(text.toUpperCase())}
                placeholder="A1B2"
                error={codeError}
                autoCapitalize="characters"
                autoComplete="one-time-code"
                autoCorrect={false}
                maxLength={CODE_LENGTH}
                style={styles.codeInput}
                editable={!isConfirming}
                onSubmitEditing={() => void onConfirmSubmit()}
              />

              {formError && (
                <ThemedText type="small" themeColor="danger">
                  {formError}
                </ThemedText>
              )}

              <SubmitButton
                label={t('settings.account.emailChange.confirmButton')}
                onPress={() => void onConfirmSubmit()}
                isPending={isConfirming}
              />

              <Pressable
                accessibilityRole="button"
                disabled={isConfirming}
                onPress={onCancelConfirm}>
                <ThemedText type="linkPrimary">
                  {t('settings.account.emailChange.cancelButton')}
                </ThemedText>
              </Pressable>
            </>
          )}
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  codeInput: {
    fontFamily: Fonts.mono,
    fontSize: 28,
    letterSpacing: 10,
    paddingLeft: Spacing.three + 10,
    paddingVertical: Spacing.three,
    textAlign: 'center',
  },
});
