import { useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import {
  getAppConfigurationsAsAdminQueryKey,
  useCreateAppConfigurationAsAdmin,
  useGetCategoriesAsAdmin,
  type CreateAppConfigurationRequestCategoryEnumKey,
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
  category: CreateAppConfigurationRequestCategoryEnumKey | undefined;
  code: string;
  label: string;
  description: string;
};

type FieldErrors = Partial<Record<keyof FormValues, string>>;

const INITIAL_VALUES: FormValues = {
  category: undefined,
  code: '',
  label: '',
  description: '',
};

export default function CreateConfigurationScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [values, setValues] = useState<FormValues>(INITIAL_VALUES);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { data: categoriesData } = useGetCategoriesAsAdmin();
  const categoryOptions = categoriesData ?? [];

  const { mutateAsync, isPending } = useCreateAppConfigurationAsAdmin({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  function validate(current: FormValues): FieldErrors {
    const errors: FieldErrors = {};

    if (!current.category) {
      errors.category = t('configurations.referenceData.create.validation.categoryRequired');
    }
    if (!current.code.trim()) {
      errors.code = t('configurations.referenceData.create.validation.codeRequired');
    } else if (current.code.trim().length > 50) {
      errors.code = t('configurations.referenceData.create.validation.codeTooLong');
    }
    if (!current.label.trim()) {
      errors.label = t('configurations.referenceData.create.validation.labelRequired');
    }

    return errors;
  }

  async function onSubmit() {
    const errors = validate(values);
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean) || !values.category) return;

    try {
      await mutateAsync({
        data: {
          category: values.category,
          code: values.code.trim(),
          label: values.label.trim(),
          description: values.description.trim() || undefined,
        },
      });
      await queryClient.invalidateQueries({
        queryKey: getAppConfigurationsAsAdminQueryKey({ filter: {}, pageable: {} }),
      });
      showToast(t('configurations.referenceData.create.toastCreated'), 'success');
      router.back();
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  return (
    <>
      <Stack.Screen options={{ title: t('configurations.referenceData.create.title') }} />
      <SettingsListScreen description={t('configurations.referenceData.create.description')}>
        <SettingsCard>
          <View style={styles.field}>
            <ThemedText type="smallBold">{t('configurations.referenceData.create.fields.category')}</ThemedText>
            <View style={styles.categoryRow}>
              {categoryOptions.map((option) => {
                const selected = values.category === option.value;
                return (
                  <Pressable
                    key={option.value}
                    accessibilityRole="button"
                    disabled={isPending}
                    onPress={() => updateField('category', option.value)}
                    style={[
                      styles.categoryChip,
                      {
                        borderColor: theme.backgroundSelected,
                        backgroundColor: selected ? theme.text : 'transparent',
                      },
                    ]}>
                    <ThemedText
                      type="small"
                      style={{ color: selected ? theme.background : theme.text }}>
                      {option.description ?? option.value}
                    </ThemedText>
                  </Pressable>
                );
              })}
            </View>
            {fieldErrors.category && (
              <ThemedText type="small" themeColor="danger">
                {fieldErrors.category}
              </ThemedText>
            )}
          </View>

          <FormTextField
            label={t('configurations.referenceData.create.fields.code')}
            value={values.code}
            onChangeText={(text) => updateField('code', text)}
            error={fieldErrors.code}
            autoCapitalize="none"
            autoCorrect={false}
            editable={!isPending}
          />

          <FormTextField
            label={t('configurations.referenceData.create.fields.label')}
            value={values.label}
            onChangeText={(text) => updateField('label', text)}
            error={fieldErrors.label}
            editable={!isPending}
          />

          <FormTextField
            label={t('configurations.referenceData.create.fields.description')}
            value={values.description}
            onChangeText={(text) => updateField('description', text)}
            multiline
            editable={!isPending}
          />

          <ThemedText type="small" themeColor="textSecondary">
            {t('configurations.referenceData.create.activeHint')}
          </ThemedText>

          {formError && (
            <ThemedText type="small" themeColor="danger">
              {formError}
            </ThemedText>
          )}

          <SubmitButton
            label={t('configurations.referenceData.create.submit')}
            onPress={() => void onSubmit()}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  field: {
    gap: Spacing.one,
  },
  categoryRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.one,
  },
  categoryChip: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
  },
});
