import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import {
  getEnrollmentsQueryKey,
  useGetCourses,
  useGetEnrollmentById,
  useUpdateEnrollmentCourses,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { Picker, type PickerOption } from '@/components/ui/picker';
import { Spinner } from '@/components/ui/spinner';

const LIST_PAGEABLE = { page: 0, size: 100 };

export default function EnrollmentCoursesScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const enrollmentId = Number(id);

  const {
    data: enrollment,
    isLoading: isEnrollmentLoading,
    isError: isEnrollmentError,
  } = useGetEnrollmentById(enrollmentId);

  const { data: coursesData } = useGetCourses(enrollment?.classId ?? 0, {
    pageable: LIST_PAGEABLE,
  });
  const courses = enrollment ? (coursesData?.items ?? []) : [];

  const [selected, setSelected] = useState<number[]>([]);

  useEffect(() => {
    if (enrollment) {
      setSelected(enrollment.courseIds ?? []);
    }
  }, [enrollment]);

  const courseOptions: PickerOption[] = courses.map((course) => ({
    label: course.name ?? '',
    value: String(course.id ?? 0),
  }));

  const { mutate, isPending } = useUpdateEnrollmentCourses({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: getEnrollmentsQueryKey({ pageable: {} }) });
        router.back();
      },
    },
  });

  function onSubmit() {
    const original = new Set(enrollment?.courseIds ?? []);
    const next = new Set(selected);
    const addCourseIds = [...next].filter((courseId) => !original.has(courseId));
    const removeCourseIds = [...original].filter((courseId) => !next.has(courseId));
    mutate({ id: enrollmentId, data: { addCourseIds, removeCourseIds } });
  }

  return (
    <>
      <Stack.Screen options={{ title: t('enrollments.coursesDialog.title') }} />
      <SettingsListScreen>
        {isEnrollmentError ? (
          <ThemedText themeColor="danger">{t('students.detail.loadError')}</ThemedText>
        ) : isEnrollmentLoading || !enrollment ? (
          <Spinner />
        ) : (
          <SettingsCard>
            <ThemedText type="small" themeColor="textSecondary">
              {t('enrollments.coursesDialog.description', {
                name: enrollment.studentName,
                className: enrollment.className,
              })}
            </ThemedText>

            {courses.length === 0 ? (
              <ThemedText type="small" themeColor="textSecondary">
                {t('enrollments.coursesDialog.noCourses')}
              </ThemedText>
            ) : (
              <Picker
                label={t('enrollments.form.fields.courses')}
                options={courseOptions}
                multiple
                searchable
                values={selected.map(String)}
                onValuesChange={(next) => setSelected(next.map(Number))}
                disabled={isPending}
              />
            )}

            <SubmitButton
              label={t('enrollments.coursesDialog.submit')}
              onPress={onSubmit}
              isPending={isPending}
            />
          </SettingsCard>
        )}
      </SettingsListScreen>
    </>
  );
}
