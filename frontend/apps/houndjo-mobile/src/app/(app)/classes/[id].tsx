import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter, type Href } from 'expo-router';
import {
  getClassByIdQueryKey,
  getClassesQueryKey,
  useDeleteClass,
  useGetClassById,
  useGetCourses,
  useGetCurrentUserPermissions,
  useUpdateClass,
  type Class,
  type Course,
} from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { AlertDialog } from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const COURSES_PAGE_SIZE = 50;

type FormValues = {
  name: string;
  description: string;
};

function mapClassToFormValues(schoolClass: Class): FormValues {
  return {
    name: schoolClass.name ?? '',
    description: schoolClass.description ?? '',
  };
}

function courseSummary(course: Course, t: ReturnType<typeof useTranslation>['t']): string {
  if (course.type === 'QURAN') {
    return t('classes.courseList.summary.quran', {
      mode: course.quranMode ? t(`classes.courseForm.quranModeOptions.${course.quranMode}`) : '',
      fromJuz: course.quranScope?.fromJuz ?? '?',
      toJuz: course.quranScope?.toJuz ?? '?',
    });
  }
  if (course.type === 'BOOK') {
    return course.bookTitle ?? '';
  }
  return '';
}

function CourseListItem({
  course,
  onPress,
}: {
  course: Course;
  onPress: () => void;
}) {
  const { t } = useTranslation();
  const theme = useTheme();

  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => pressed && styles.pressed}>
      <SettingsCard style={styles.courseCard}>
        <View style={styles.courseHeaderRow}>
          <ThemedText type="smallBold" numberOfLines={1} style={styles.courseName}>
            {course.name}
          </ThemedText>
          <View style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
            <ThemedText type="small" themeColor="textSecondary">
              {t(`classes.courseForm.typeOptions.${course.type}`)}
            </ThemedText>
          </View>
        </View>
        {courseSummary(course, t) !== '' && (
          <ThemedText type="small" themeColor="textSecondary" numberOfLines={1}>
            {courseSummary(course, t)}
          </ThemedText>
        )}
      </SettingsCard>
    </Pressable>
  );
}

export default function ClassDetailScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const classId = Number(id);

  const { data: permissions } = useGetCurrentUserPermissions();
  const canUpdateClasses = (permissions ?? []).some((permission) => permission.code === 'class:update');
  const canDeleteClasses = (permissions ?? []).some((permission) => permission.code === 'class:delete');
  const canCreateCourses = (permissions ?? []).some((permission) => permission.code === 'course:create');

  const {
    data: schoolClass,
    isLoading: isClassLoading,
    isError: isClassError,
  } = useGetClassById(classId);

  const { data: coursesData, isLoading: isCoursesLoading } = useGetCourses(classId, {
    pageable: { page: 0, size: COURSES_PAGE_SIZE },
  });
  const courses = coursesData?.items ?? [];

  const [values, setValues] = useState<FormValues | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ name?: string }>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [isConfirmingDelete, setIsConfirmingDelete] = useState(false);

  useEffect(() => {
    if (schoolClass) {
      setValues(mapClassToFormValues(schoolClass));
    }
  }, [schoolClass]);

  const { mutateAsync: updateClass, isPending: isUpdating } = useUpdateClass({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  const { mutate: deleteClass, isPending: isDeleting } = useDeleteClass({
    mutation: {
      meta: { skipGlobalErrorToast: true },
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: getClassesQueryKey({ pageable: {} }) });
        router.back();
      },
      onError: (error) => {
        setFormError(
          error instanceof AxiosError ? extractApiErrorMessage(error, t('errors.generic')) : t('errors.generic')
        );
      },
    },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => (current ? { ...current, [key]: next } : current));
  }

  async function onSubmit() {
    if (!values) return;

    const name = values.name.trim();
    if (!name) {
      setFieldErrors({ name: t('classes.detail.validation.nameRequired') });
      return;
    }
    setFieldErrors({});
    setFormError(null);

    try {
      const updated = await updateClass({
        id: classId,
        data: { name, description: values.description.trim() || undefined },
      });
      queryClient.setQueryData(getClassByIdQueryKey(classId), updated);
      await queryClient.invalidateQueries({ queryKey: getClassesQueryKey({ pageable: {} }) });
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
      <Stack.Screen options={{ title: schoolClass?.name || t('classes.detail.title') }} />
      <SettingsListScreen>
        {isClassError ? (
          <ThemedText themeColor="danger">{t('classes.detail.loadError')}</ThemedText>
        ) : isClassLoading || !values || !schoolClass ? (
          <Spinner />
        ) : (
          <>
            <SettingsCard>
              <FormTextField
                label={t('classes.detail.fields.name')}
                value={values.name}
                onChangeText={(text) => updateField('name', text)}
                error={fieldErrors.name}
                editable={!isUpdating && canUpdateClasses}
              />

              <FormTextField
                label={t('classes.detail.fields.description')}
                value={values.description}
                onChangeText={(text) => updateField('description', text)}
                multiline
                editable={!isUpdating && canUpdateClasses}
              />

              {formError && (
                <ThemedText type="small" themeColor="danger">
                  {formError}
                </ThemedText>
              )}

              {canUpdateClasses && (
                <SubmitButton
                  label={t('classes.detail.submit')}
                  onPress={() => void onSubmit()}
                  isPending={isUpdating}
                />
              )}
            </SettingsCard>

            {canDeleteClasses && (
              <Button variant="destructive" loading={isDeleting} onPress={() => setIsConfirmingDelete(true)}>
                {t('classes.detail.delete')}
              </Button>
            )}

            <View style={styles.coursesHeaderRow}>
              <ThemedText type="smallBold">{t('classes.detail.coursesTitle')}</ThemedText>
              {canCreateCourses && (
                <Button
                  size="sm"
                  onPress={() =>
                    router.push({
                      pathname: '/classes/courses/create',
                      params: { classId: String(classId) },
                    } as Href)
                  }>
                  {t('classes.detail.addCourse')}
                </Button>
              )}
            </View>

            {isCoursesLoading ? (
              <Spinner />
            ) : courses.length === 0 ? (
              <ThemedText type="small" themeColor="textSecondary">
                {t('classes.courseList.noResults')}
              </ThemedText>
            ) : (
              <View style={styles.coursesList}>
                {courses.map((course) => (
                  <CourseListItem
                    key={course.id}
                    course={course}
                    onPress={() =>
                      router.push({
                        pathname: `/classes/courses/${course.id ?? 0}`,
                        params: { classId: String(classId) },
                      } as Href)
                    }
                  />
                ))}
              </View>
            )}
          </>
        )}
      </SettingsListScreen>

      {schoolClass && canDeleteClasses && (
        <AlertDialog
          isVisible={isConfirmingDelete}
          onClose={() => setIsConfirmingDelete(false)}
          title={t('classes.detail.deleteConfirmTitle')}
          description={t('classes.detail.deleteConfirmMessage', { name: schoolClass.name })}
          confirmText={t('classes.detail.delete')}
          cancelText={t('common.cancel')}
          onConfirm={() => deleteClass({ id: classId })}
        />
      )}
    </>
  );
}

const styles = StyleSheet.create({
  pressed: {
    opacity: 0.7,
  },
  chip: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
  },
  coursesHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.two,
  },
  coursesList: {
    gap: Spacing.two,
  },
  courseCard: {
    gap: Spacing.two,
  },
  courseHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.two,
  },
  courseName: {
    flex: 1,
  },
});
