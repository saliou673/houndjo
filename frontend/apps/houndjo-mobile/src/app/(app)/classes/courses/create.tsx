import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import { getClassByIdQueryKey, getCoursesQueryKey, useCreateCourse } from '@api-client';

import {
  buildCourseRequestData,
  CourseFormFields,
  INITIAL_COURSE_FORM_VALUES,
  validateCourseForm,
  type CourseFormFieldErrors,
  type CourseFormValues,
} from '@/components/course-form-fields';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { extractApiErrorMessage } from '@/lib/api-error';

export default function CreateCourseScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { classId: classIdParam } = useLocalSearchParams<{ classId: string }>();
  const classId = Number(classIdParam);

  const [values, setValues] = useState<CourseFormValues>(INITIAL_COURSE_FORM_VALUES);
  const [fieldErrors, setFieldErrors] = useState<CourseFormFieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useCreateCourse({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof CourseFormValues>(key: K, next: CourseFormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  async function onSubmit() {
    const errors = validateCourseForm(values, t);
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      await mutateAsync({ classId, data: buildCourseRequestData(values) });
      await queryClient.invalidateQueries({
        queryKey: getCoursesQueryKey(classId, { pageable: {} }),
      });
      await queryClient.invalidateQueries({ queryKey: getClassByIdQueryKey(classId) });
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
      <Stack.Screen options={{ title: t('classes.courseForm.addTitle') }} />
      <SettingsListScreen description={t('classes.courseForm.addDescription')}>
        <SettingsCard>
          <CourseFormFields
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
            label={t('classes.courseForm.submitAdd')}
            onPress={() => void onSubmit()}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
