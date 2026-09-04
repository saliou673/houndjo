import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import { getStudentsQueryKey, useCreateStudent } from '@api-client';

import {
  buildStudentRequestData,
  INITIAL_STUDENT_FORM_VALUES,
  StudentFormFields,
  validateStudentForm,
  type StudentFormFieldErrors,
  type StudentFormValues,
} from '@/components/student-form-fields';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { extractApiErrorMessage } from '@/lib/api-error';

export default function CreateStudentScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [values, setValues] = useState<StudentFormValues>(INITIAL_STUDENT_FORM_VALUES);
  const [fieldErrors, setFieldErrors] = useState<StudentFormFieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useCreateStudent({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof StudentFormValues>(key: K, next: StudentFormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  async function onSubmit() {
    const errors = validateStudentForm(values, t);
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      await mutateAsync({ data: buildStudentRequestData(values) });
      await queryClient.invalidateQueries({ queryKey: getStudentsQueryKey({ pageable: {} }) });
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
      <Stack.Screen options={{ title: t('students.form.addTitle') }} />
      <SettingsListScreen description={t('students.form.addDescription')}>
        <SettingsCard>
          <StudentFormFields
            values={values}
            errors={fieldErrors}
            disabled={isPending}
            onChange={updateField}
          />

          {formError && (
            <ThemedText type="small" themeColor="danger">
              {formError}
            </ThemedText>
          )}

          <SubmitButton
            label={t('students.form.submitAdd')}
            onPress={() => void onSubmit()}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
