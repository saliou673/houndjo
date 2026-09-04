import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import { getClassesQueryKey, useCreateClass } from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { extractApiErrorMessage } from '@/lib/api-error';

type FormValues = {
  name: string;
  description: string;
};

type FieldErrors = {
  name?: string;
};

const INITIAL_VALUES: FormValues = {
  name: '',
  description: '',
};

export default function CreateClassScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [values, setValues] = useState<FormValues>(INITIAL_VALUES);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useCreateClass({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  async function onSubmit() {
    const name = values.name.trim();
    if (!name) {
      setFieldErrors({ name: t('classes.create.validation.nameRequired') });
      return;
    }
    setFieldErrors({});
    setFormError(null);

    try {
      await mutateAsync({
        data: {
          name,
          description: values.description.trim() || undefined,
        },
      });
      await queryClient.invalidateQueries({ queryKey: getClassesQueryKey({ pageable: {} }) });
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
      <Stack.Screen options={{ title: t('classes.create.title') }} />
      <SettingsListScreen description={t('classes.create.description')}>
        <SettingsCard>
          <FormTextField
            label={t('classes.create.fields.name')}
            value={values.name}
            onChangeText={(text) => updateField('name', text)}
            error={fieldErrors.name}
            editable={!isPending}
          />

          <FormTextField
            label={t('classes.create.fields.description')}
            value={values.description}
            onChangeText={(text) => updateField('description', text)}
            multiline
            editable={!isPending}
          />

          {formError && (
            <ThemedText type="small" themeColor="danger">
              {formError}
            </ThemedText>
          )}

          <SubmitButton
            label={t('classes.create.submit')}
            onPress={() => void onSubmit()}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
