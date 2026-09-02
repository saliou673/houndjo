import { useEffect, useState } from 'react';
import { ActivityIndicator, Alert, Pressable, StyleSheet, Switch, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import {
  getAppConfigurationByIdAsAdminQueryKey,
  getAppConfigurationsAsAdminQueryKey,
  useDelete,
  useGetAppConfigurationByIdAsAdmin,
  useUpdateAppConfigurationAsAdmin,
  type AppConfiguration,
} from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { extractApiErrorMessage } from '@/lib/api-error';

type FormValues = {
  code: string;
  label: string;
  description: string;
  active: boolean;
};

function mapConfigurationToFormValues(configuration: AppConfiguration): FormValues {
  return {
    code: configuration.code ?? '',
    label: configuration.label ?? '',
    description: configuration.description ?? '',
    active: configuration.active ?? true,
  };
}

export default function ConfigurationDetailScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const configurationId = Number(id);

  const {
    data: configuration,
    isLoading: isConfigurationLoading,
    isError: isConfigurationError,
  } = useGetAppConfigurationByIdAsAdmin(configurationId);

  const [values, setValues] = useState<FormValues | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ code?: string; label?: string }>({});
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (configuration) {
      setValues(mapConfigurationToFormValues(configuration));
    }
  }, [configuration]);

  const { mutateAsync, isPending } = useUpdateAppConfigurationAsAdmin({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });
  const { mutate: deleteConfiguration, isPending: isDeleting } = useDelete({
    mutation: {
      meta: { skipGlobalErrorToast: true },
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getAppConfigurationsAsAdminQueryKey({ filter: {}, pageable: {} }),
        });
        showToast(t('configurations.referenceData.detail.toastDeleted'), 'success');
        router.back();
      },
      onError: (error) => {
        showToast(
          error instanceof AxiosError ? extractApiErrorMessage(error, t('errors.generic')) : t('errors.generic'),
          'error'
        );
      },
    },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => (current ? { ...current, [key]: next } : current));
  }

  async function onSubmit() {
    if (!values) return;

    const errors: { code?: string; label?: string } = {};
    if (!values.code.trim()) {
      errors.code = t('configurations.referenceData.detail.validation.codeRequired');
    } else if (values.code.trim().length > 50) {
      errors.code = t('configurations.referenceData.detail.validation.codeTooLong');
    }
    if (!values.label.trim()) {
      errors.label = t('configurations.referenceData.detail.validation.labelRequired');
    }
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      const updatedConfiguration = await mutateAsync({
        id: configurationId,
        data: {
          code: values.code.trim(),
          label: values.label.trim(),
          description: values.description.trim() || undefined,
          active: values.active,
        },
      });
      queryClient.setQueryData(
        getAppConfigurationByIdAsAdminQueryKey(configurationId),
        updatedConfiguration
      );
      await queryClient.invalidateQueries({
        queryKey: getAppConfigurationsAsAdminQueryKey({ filter: {}, pageable: {} }),
      });
      showToast(t('configurations.referenceData.detail.toastUpdated'), 'success');
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  function handleDelete() {
    if (!configuration) return;

    Alert.alert(
      t('configurations.referenceData.detail.deleteConfirmTitle'),
      t('configurations.referenceData.detail.deleteConfirmMessage', { code: configuration.code }),
      [
        { text: t('common.cancel'), style: 'cancel' },
        {
          text: t('configurations.referenceData.detail.delete'),
          style: 'destructive',
          onPress: () => deleteConfiguration({ id: configurationId }),
        },
      ]
    );
  }

  return (
    <>
      <Stack.Screen options={{ title: configuration?.code || t('configurations.referenceData.detail.title') }} />
      <SettingsListScreen>
        {isConfigurationLoading || !values ? (
          <ActivityIndicator />
        ) : isConfigurationError || !configuration ? (
          <ThemedText themeColor="danger">{t('configurations.referenceData.detail.loadError')}</ThemedText>
        ) : (
          <>
            <SettingsCard>
              <View style={styles.readOnlyRow}>
                <ThemedText type="small" themeColor="textSecondary">
                  {t('configurations.referenceData.detail.fields.category')}
                </ThemedText>
                <ThemedText>{configuration.category}</ThemedText>
              </View>

              <FormTextField
                label={t('configurations.referenceData.detail.fields.code')}
                value={values.code}
                onChangeText={(text) => updateField('code', text)}
                error={fieldErrors.code}
                autoCapitalize="none"
                autoCorrect={false}
                editable={!isPending}
              />

              <FormTextField
                label={t('configurations.referenceData.detail.fields.label')}
                value={values.label}
                onChangeText={(text) => updateField('label', text)}
                error={fieldErrors.label}
                editable={!isPending}
              />

              <FormTextField
                label={t('configurations.referenceData.detail.fields.description')}
                value={values.description}
                onChangeText={(text) => updateField('description', text)}
                multiline
                editable={!isPending}
              />

              <View style={styles.switchRow}>
                <ThemedText>{t('configurations.referenceData.detail.fields.active')}</ThemedText>
                <Switch
                  value={values.active}
                  disabled={isPending}
                  onValueChange={(next) => updateField('active', next)}
                />
              </View>

              {formError && (
                <ThemedText type="small" themeColor="danger">
                  {formError}
                </ThemedText>
              )}

              <SubmitButton
                label={t('configurations.referenceData.detail.submit')}
                onPress={() => void onSubmit()}
                isPending={isPending}
              />
            </SettingsCard>

            <Pressable
              accessibilityRole="button"
              disabled={isDeleting}
              onPress={handleDelete}
              style={({ pressed }) => [
                styles.deleteButton,
                { backgroundColor: theme.danger },
                (pressed || isDeleting) && styles.pressed,
              ]}>
              {isDeleting ? (
                <ActivityIndicator color={theme.background} />
              ) : (
                <ThemedText type="smallBold" style={{ color: theme.background }}>
                  {t('configurations.referenceData.detail.delete')}
                </ThemedText>
              )}
            </Pressable>
          </>
        )}
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  readOnlyRow: {
    gap: Spacing.half,
  },
  switchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  deleteButton: {
    marginTop: Spacing.two,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: Spacing.two,
    paddingVertical: Spacing.three,
    minHeight: 48,
  },
  pressed: {
    opacity: 0.7,
  },
});
