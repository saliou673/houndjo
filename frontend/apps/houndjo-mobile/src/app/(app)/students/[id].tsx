import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import {
  getStudentByIdQueryKey,
  getStudentsQueryKey,
  useDeleteStudent,
  useGetStudentById,
  useGetCurrentUserPermissions,
  useUpdateStudent,
  type Student,
} from '@api-client';

import {
  buildStudentRequestData,
  StudentFormFields,
  validateStudentForm,
  type StudentFormFieldErrors,
  type StudentFormValues,
} from '@/components/student-form-fields';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { AlertDialog } from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { extractApiErrorMessage } from '@/lib/api-error';

function mapStudentToFormValues(student: Student): StudentFormValues {
  return {
    firstName: student.firstName ?? '',
    lastName: student.lastName ?? '',
    birthDate: student.birthDate ?? '',
    gender: student.gender,
    guardianName: student.guardianName ?? '',
    guardianPhone: student.guardianPhone ?? '',
  };
}

export default function StudentDetailScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const studentId = Number(id);

  const { data: permissions } = useGetCurrentUserPermissions();
  const canUpdateStudents = (permissions ?? []).some((permission) => permission.code === 'student:update');
  const canDeleteStudents = (permissions ?? []).some((permission) => permission.code === 'student:delete');

  const {
    data: student,
    isLoading: isStudentLoading,
    isError: isStudentError,
  } = useGetStudentById(studentId);

  const [values, setValues] = useState<StudentFormValues | null>(null);
  const [fieldErrors, setFieldErrors] = useState<StudentFormFieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [isConfirmingDelete, setIsConfirmingDelete] = useState(false);

  useEffect(() => {
    if (student) {
      setValues(mapStudentToFormValues(student));
    }
  }, [student]);

  const { mutateAsync: updateStudent, isPending: isUpdating } = useUpdateStudent({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  const { mutate: deleteStudent, isPending: isDeleting } = useDeleteStudent({
    mutation: {
      meta: { skipGlobalErrorToast: true },
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: getStudentsQueryKey({ pageable: {} }) });
        router.back();
      },
      onError: (error) => {
        setFormError(
          error instanceof AxiosError ? extractApiErrorMessage(error, t('errors.generic')) : t('errors.generic')
        );
      },
    },
  });

  function updateField<K extends keyof StudentFormValues>(key: K, next: StudentFormValues[K]) {
    setValues((current) => (current ? { ...current, [key]: next } : current));
  }

  async function onSubmit() {
    if (!values) return;

    const errors = validateStudentForm(values, t);
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      const updated = await updateStudent({ id: studentId, data: buildStudentRequestData(values) });
      queryClient.setQueryData(getStudentByIdQueryKey(studentId), updated);
      await queryClient.invalidateQueries({ queryKey: getStudentsQueryKey({ pageable: {} }) });
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  const studentName = student ? `${student.firstName ?? ''} ${student.lastName ?? ''}`.trim() : '';

  return (
    <>
      <Stack.Screen options={{ title: studentName || t('students.detail.title') }} />
      <SettingsListScreen>
        {isStudentError ? (
          <ThemedText themeColor="danger">{t('students.detail.loadError')}</ThemedText>
        ) : isStudentLoading || !values || !student ? (
          <Spinner />
        ) : (
          <>
            <SettingsCard>
              <StudentFormFields
                values={values}
                errors={fieldErrors}
                disabled={isUpdating || !canUpdateStudents}
                onChange={updateField}
              />

              {formError && (
                <ThemedText type="small" themeColor="danger">
                  {formError}
                </ThemedText>
              )}

              {canUpdateStudents && (
                <SubmitButton
                  label={t('students.form.submitEdit')}
                  onPress={() => void onSubmit()}
                  isPending={isUpdating}
                />
              )}
            </SettingsCard>

            {canDeleteStudents && (
              <Button variant="destructive" loading={isDeleting} onPress={() => setIsConfirmingDelete(true)}>
                {t('students.deleteDialog.confirmButton')}
              </Button>
            )}
          </>
        )}
      </SettingsListScreen>

      {student && canDeleteStudents && (
        <AlertDialog
          isVisible={isConfirmingDelete}
          onClose={() => setIsConfirmingDelete(false)}
          title={t('students.deleteDialog.title')}
          description={t('students.deleteDialog.confirmMessage', { name: studentName })}
          confirmText={t('students.deleteDialog.confirmButton')}
          cancelText={t('common.cancel')}
          onConfirm={() => deleteStudent({ id: studentId })}
        />
      )}
    </>
  );
}
