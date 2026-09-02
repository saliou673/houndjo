import { useState } from 'react';
import { Linking, Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import QRCode from 'react-native-qrcode-svg';
import {
  getUserDetailsQueryKey,
  twoFactorSetupRequestTypeEnum,
  useConfirm2FactorSetup,
  useDisable2Factor,
  useGetUserDetails,
  useInit2FactorSetup,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { FormTextField } from '@/components/form-text-field';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const QR_SIZE = 176;

// The generated client types this response as a bare `object` since its
// shape depends on the setup `type` (see TwoFactorController#init2FactorSetup);
// TOTP always returns these two fields.
type TotpSetupResponse = { secret: string; otpAuthUri: string };

const CODE_LENGTH = 6;

export default function TwoFactorScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const { data: user } = useGetUserDetails();
  const isEnabled = user?.twoFactorEnabled ?? false;

  const [setup, setSetup] = useState<TotpSetupResponse | null>(null);
  const [code, setCode] = useState('');
  const [codeError, setCodeError] = useState<string | undefined>();
  const [formError, setFormError] = useState<string | null>(null);
  const [disablePassword, setDisablePassword] = useState('');
  const [disablePasswordError, setDisablePasswordError] = useState<string | undefined>();

  const { mutateAsync: initSetup, isPending: isInitiating } = useInit2FactorSetup({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });
  const { mutateAsync: confirmSetup, isPending: isConfirming } = useConfirm2FactorSetup({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });
  const { mutateAsync: disable2Factor, isPending: isDisabling } = useDisable2Factor({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  async function onStartSetup() {
    setFormError(null);

    try {
      const response = await initSetup({ data: { type: twoFactorSetupRequestTypeEnum.TOTP } });
      setSetup(response as TotpSetupResponse);
    } catch {
      setFormError(t('settings.account.twoFactorSetup.startError'));
    }
  }

  async function onOpenAuthenticator() {
    if (!setup) return;

    try {
      await Linking.openURL(setup.otpAuthUri);
    } catch {
      // No app registered for the otpauth:// scheme on this device - the
      // secret below is the fallback for manual entry.
    }
  }

  async function onConfirmSubmit() {
    setCodeError(undefined);
    setFormError(null);

    const trimmed = code.trim();
    if (!trimmed) {
      setCodeError(t('settings.account.twoFactorSetup.codeRequired'));
      return;
    }

    try {
      await confirmSetup({ data: { code: trimmed } });
      await queryClient.invalidateQueries({ queryKey: getUserDetailsQueryKey() });
      showToast(t('settings.account.twoFactorSetup.toastEnabled'), 'success');
      // The user details refetch above can 401 and force a logout (e.g. a
      // stale session), which tears down this screen's navigator before we
      // get here - going back at that point has nothing to go back to.
      if (router.canGoBack()) {
        router.back();
      }
    } catch (error) {
      if (error instanceof AxiosError) {
        setCodeError(
          extractApiErrorMessage(error, t('settings.account.twoFactorSetup.invalidCode'))
        );
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  async function onDisableSubmit() {
    setDisablePasswordError(undefined);
    setFormError(null);

    if (!disablePassword) {
      setDisablePasswordError(t('settings.account.twoFactorDisable.passwordRequired'));
      return;
    }

    try {
      await disable2Factor({ data: { currentPassword: disablePassword } });
      await queryClient.invalidateQueries({ queryKey: getUserDetailsQueryKey() });
      showToast(t('settings.account.twoFactorDisable.toastDisabled'), 'success');
      // The user details refetch above can 401 and force a logout (e.g. a
      // stale session), which tears down this screen's navigator before we
      // get here - going back at that point has nothing to go back to.
      if (router.canGoBack()) {
        router.back();
      }
    } catch (error) {
      if (error instanceof AxiosError) {
        const status = error.response?.status;

        if (status === 403 || status === 409) {
          setDisablePasswordError(
            extractApiErrorMessage(error, t('settings.account.twoFactorDisable.invalidPassword'))
          );
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
      <Stack.Screen options={{ title: t('settings.account.twoFactor') }} />
      <SettingsListScreen
        description={
          isEnabled
            ? t('settings.account.twoFactorSetup.enabledDescription')
            : t('settings.account.twoFactorSetup.description')
        }>
        {isEnabled ? (
          <SettingsCard>
            <ThemedText>{t('settings.account.twoFactorDisable.description')}</ThemedText>

            <FormTextField
              label={t('settings.account.twoFactorDisable.currentPasswordLabel')}
              value={disablePassword}
              onChangeText={setDisablePassword}
              error={disablePasswordError}
              autoCapitalize="none"
              autoComplete="current-password"
              autoCorrect={false}
              secureTextEntry
              editable={!isDisabling}
              onSubmitEditing={() => void onDisableSubmit()}
            />

            {formError && (
              <ThemedText type="small" themeColor="danger">
                {formError}
              </ThemedText>
            )}

            <SubmitButton
              label={t('settings.account.twoFactorDisable.submit')}
              onPress={() => void onDisableSubmit()}
              isPending={isDisabling}
            />
          </SettingsCard>
        ) : !setup ? (
          <SettingsCard>
            {formError && (
              <ThemedText type="small" themeColor="danger">
                {formError}
              </ThemedText>
            )}
            <SubmitButton
              label={t('settings.account.twoFactorSetup.startButton')}
              onPress={() => void onStartSetup()}
              isPending={isInitiating}
            />
          </SettingsCard>
        ) : (
          <SettingsCard>
            <View style={styles.qrBlock}>
              <ThemedText type="small" themeColor="textSecondary">
                {t('settings.account.twoFactorSetup.scanHint')}
              </ThemedText>
              <View style={styles.qrFrame}>
                <QRCode value={setup.otpAuthUri} size={QR_SIZE} backgroundColor="#ffffff" />
              </View>
            </View>

            <Pressable accessibilityRole="button" onPress={() => void onOpenAuthenticator()}>
              <ThemedText type="linkPrimary">
                {t('settings.account.twoFactorSetup.openAuthenticator')}
              </ThemedText>
            </Pressable>

            <View style={styles.secretBlock}>
              <ThemedText type="small" themeColor="textSecondary">
                {t('settings.account.twoFactorSetup.manualEntryHint')}
              </ThemedText>
              <ThemedText type="code" style={styles.secret} selectable>
                {setup.secret}
              </ThemedText>
            </View>

            <FormTextField
              label={t('settings.account.twoFactorSetup.codeLabel')}
              value={code}
              onChangeText={setCode}
              placeholder="123456"
              error={codeError}
              keyboardType="number-pad"
              autoComplete="one-time-code"
              maxLength={CODE_LENGTH}
              editable={!isConfirming}
              onSubmitEditing={() => void onConfirmSubmit()}
            />

            {formError && (
              <ThemedText type="small" themeColor="danger">
                {formError}
              </ThemedText>
            )}

            <SubmitButton
              label={t('settings.account.twoFactorSetup.confirmButton')}
              onPress={() => void onConfirmSubmit()}
              isPending={isConfirming}
            />
          </SettingsCard>
        )}
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  qrBlock: {
    gap: Spacing.two,
    alignItems: 'flex-start',
  },
  qrFrame: {
    padding: Spacing.two,
    borderRadius: Spacing.two,
    backgroundColor: '#ffffff',
  },
  secretBlock: {
    gap: Spacing.one,
  },
  secret: {
    fontSize: 18,
    letterSpacing: 2,
  },
});
