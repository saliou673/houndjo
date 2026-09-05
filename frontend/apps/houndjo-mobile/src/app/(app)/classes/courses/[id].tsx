import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter, type Href } from 'expo-router';
import {
  getClassByIdQueryKey,
  getCourseByIdQueryKey,
  getCoursesQueryKey,
  useDeleteCourse,
  useGetCourseById,
  useGetCurrentUserPermissions,
  useUpdateCourse,
  type Course,
} from '@api-client';

import {
  buildCourseRequestData,
  CourseFormFields,
  validateCourseForm,
  type CourseFormFieldErrors,
  type CourseFormValues,
} from '@/components/course-form-fields';
import { CoursePaceCard } from '@/components/course-pace-card';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { AlertDialog } from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { extractApiErrorMessage } from '@/lib/api-error';

function mapCourseToFormValues(course: Course): CourseFormValues {
  return {
    name: course.name ?? '',
    type: course.type ?? 'QAIDA',
    description: course.description ?? '',
    qaidaLessons: course.qaidaLessons?.join('\n') ?? '',
    quranMode: course.quranMode,
    quranScopeFromJuz: course.quranScope?.fromJuz,
    quranScopeToJuz: course.quranScope?.toJuz,
    bookTitle: course.bookTitle ?? '',
    bookTotalChapters: course.bookTotalChapters != null ? String(course.bookTotalChapters) : '',
    bookTotalPages: course.bookTotalPages != null ? String(course.bookTotalPages) : '',
  };
}

export default function CourseDetailScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id, classId: classIdParam } = useLocalSearchParams<{ id: string; classId: string }>();
  const courseId = Number(id);
  const classId = Number(classIdParam);

  const { data: permissions } = useGetCurrentUserPermissions();
  const canUpdateCourses = (permissions ?? []).some((permission) => permission.code === 'course:update');
  const canDeleteCourses = (permissions ?? []).some((permission) => permission.code === 'course:delete');
  const canReadSessions = (permissions ?? []).some((permission) => permission.code === 'session:read');
  const canCreateSessions = (permissions ?? []).some((permission) => permission.code === 'session:create');
  const canAccessSessions = canReadSessions || canCreateSessions;

  const {
    data: course,
    isLoading: isCourseLoading,
    isError: isCourseError,
  } = useGetCourseById(classId, courseId);

  const [values, setValues] = useState<CourseFormValues | null>(null);
  const [fieldErrors, setFieldErrors] = useState<CourseFormFieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [isConfirmingDelete, setIsConfirmingDelete] = useState(false);

  useEffect(() => {
    if (course) {
      setValues(mapCourseToFormValues(course));
    }
  }, [course]);

  const { mutateAsync: updateCourse, isPending: isUpdating } = useUpdateCourse({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  const { mutate: deleteCourse, isPending: isDeleting } = useDeleteCourse({
    mutation: {
      meta: { skipGlobalErrorToast: true },
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getCoursesQueryKey(classId, { pageable: {} }),
        });
        await queryClient.invalidateQueries({ queryKey: getClassByIdQueryKey(classId) });
        router.back();
      },
      onError: (error) => {
        setFormError(
          error instanceof AxiosError ? extractApiErrorMessage(error, t('errors.generic')) : t('errors.generic')
        );
      },
    },
  });

  function updateField<K extends keyof CourseFormValues>(key: K, next: CourseFormValues[K]) {
    setValues((current) => (current ? { ...current, [key]: next } : current));
  }

  async function onSubmit() {
    if (!values) return;

    const errors = validateCourseForm(values, t);
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      const updated = await updateCourse({
        classId,
        id: courseId,
        data: buildCourseRequestData(values),
      });
      queryClient.setQueryData(getCourseByIdQueryKey(classId, courseId), updated);
      await queryClient.invalidateQueries({
        queryKey: getCoursesQueryKey(classId, { pageable: {} }),
      });
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
      <Stack.Screen options={{ title: course?.name || t('classes.courseForm.editTitle') }} />
      <SettingsListScreen>
        {isCourseError ? (
          <ThemedText themeColor="danger">{t('classes.detail.loadError')}</ThemedText>
        ) : isCourseLoading || !values || !course ? (
          <Spinner />
        ) : (
          <>
            <SettingsCard>
              <CourseFormFields
                values={values}
                errors={fieldErrors}
                disabled={isUpdating || !canUpdateCourses}
                onChange={updateField}
              />

              {formError && (
                <ThemedText type="small" themeColor="danger">
                  {formError}
                </ThemedText>
              )}

              {canUpdateCourses && (
                <SubmitButton
                  label={t('classes.courseForm.submitEdit')}
                  onPress={() => void onSubmit()}
                  isPending={isUpdating}
                />
              )}
            </SettingsCard>

            <CoursePaceCard courseId={courseId} courseType={course.type ?? 'QAIDA'} canUpdate={canUpdateCourses} />

            {canAccessSessions && (
              <Button
                variant="outline"
                onPress={() =>
                  router.push({
                    pathname: '/classes/courses/sessions',
                    params: { classId: String(classId), courseId: String(courseId) },
                  } as Href)
                }>
                {t('classes.sessions.manageAction')}
              </Button>
            )}

            {canDeleteCourses && (
              <Button variant="destructive" loading={isDeleting} onPress={() => setIsConfirmingDelete(true)}>
                {t('classes.courseDeleteDialog.confirmButton')}
              </Button>
            )}
          </>
        )}
      </SettingsListScreen>

      {course && canDeleteCourses && (
        <AlertDialog
          isVisible={isConfirmingDelete}
          onClose={() => setIsConfirmingDelete(false)}
          title={t('classes.courseDeleteDialog.title')}
          description={t('classes.courseDeleteDialog.confirmMessage', { name: course.name })}
          confirmText={t('classes.courseDeleteDialog.confirmButton')}
          cancelText={t('common.cancel')}
          onConfirm={() => deleteCourse({ classId, id: courseId })}
        />
      )}
    </>
  );
}
