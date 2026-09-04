import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import {
  getEnrollmentsQueryKey,
  useCreateEnrollment,
  useGetClasses,
  useGetCourses,
  useGetStudents,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { Picker, type PickerOption } from '@/components/ui/picker';
import { extractApiErrorMessage } from '@/lib/api-error';

const LIST_PAGEABLE = { page: 0, size: 100 };

type FormValues = {
  studentId?: number;
  classId?: number;
  courseIds: number[];
};

type FieldErrors = Partial<Record<'studentId' | 'classId', string>>;

const INITIAL_VALUES: FormValues = {
  studentId: undefined,
  classId: undefined,
  courseIds: [],
};

export default function CreateEnrollmentScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [values, setValues] = useState<FormValues>(INITIAL_VALUES);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { data: studentsData } = useGetStudents({ pageable: LIST_PAGEABLE });
  const { data: classesData } = useGetClasses({ pageable: LIST_PAGEABLE });
  const { data: coursesData } = useGetCourses(values.classId ?? 0, { pageable: LIST_PAGEABLE });

  const students = studentsData?.items ?? [];
  const classes = classesData?.items ?? [];
  const courses = values.classId != null ? (coursesData?.items ?? []) : [];

  const studentOptions: PickerOption[] = students.map((student) => ({
    label: `${student.firstName ?? ''} ${student.lastName ?? ''}`.trim(),
    value: String(student.id ?? 0),
  }));

  const classOptions: PickerOption[] = classes.map((schoolClass) => ({
    label: schoolClass.name ?? '',
    value: String(schoolClass.id ?? 0),
  }));

  const courseOptions: PickerOption[] = courses.map((course) => ({
    label: course.name ?? '',
    value: String(course.id ?? 0),
  }));

  const { mutateAsync, isPending } = useCreateEnrollment({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  async function onSubmit() {
    const errors: FieldErrors = {};
    if (values.studentId == null) errors.studentId = t('enrollments.form.validation.studentRequired');
    if (values.classId == null) errors.classId = t('enrollments.form.validation.classRequired');
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean) || values.studentId == null || values.classId == null) return;

    try {
      await mutateAsync({
        data: {
          studentId: values.studentId,
          classId: values.classId,
          courseIds: values.courseIds,
        },
      });
      await queryClient.invalidateQueries({ queryKey: getEnrollmentsQueryKey({ pageable: {} }) });
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
      <Stack.Screen options={{ title: t('enrollments.form.addTitle') }} />
      <SettingsListScreen description={t('enrollments.form.addDescription')}>
        <SettingsCard>
          <Picker
            label={t('enrollments.form.fields.student')}
            placeholder={t('enrollments.form.fields.studentPlaceholder')}
            options={studentOptions}
            searchable
            value={values.studentId != null ? String(values.studentId) : undefined}
            onValueChange={(value) => updateField('studentId', Number(value))}
            disabled={isPending}
            error={fieldErrors.studentId}
          />

          <Picker
            label={t('enrollments.form.fields.class')}
            placeholder={t('enrollments.form.fields.classPlaceholder')}
            options={classOptions}
            searchable
            value={values.classId != null ? String(values.classId) : undefined}
            onValueChange={(value) => {
              updateField('classId', Number(value));
              updateField('courseIds', []);
            }}
            disabled={isPending}
            error={fieldErrors.classId}
          />

          {values.classId != null && courses.length === 0 && (
            <ThemedText type="small" themeColor="textSecondary">
              {t('enrollments.form.fields.noCourses')}
            </ThemedText>
          )}

          {values.classId != null && courses.length > 0 && (
            <Picker
              label={t('enrollments.form.fields.courses')}
              placeholder={t('enrollments.form.fields.coursesPlaceholder')}
              options={courseOptions}
              multiple
              searchable
              values={values.courseIds.map(String)}
              onValuesChange={(next) => updateField('courseIds', next.map(Number))}
              disabled={isPending}
            />
          )}

          {formError && (
            <ThemedText type="small" themeColor="danger">
              {formError}
            </ThemedText>
          )}

          <SubmitButton
            label={t('enrollments.form.submitAdd')}
            onPress={() => void onSubmit()}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
