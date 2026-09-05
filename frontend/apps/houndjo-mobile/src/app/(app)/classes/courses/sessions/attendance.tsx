import { useMemo, useState } from 'react';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams } from 'expo-router';
import {
  getAttendanceQueryKey,
  useActiveCourseEnrollments,
  useGetAttendance,
  useRecordBulkAttendance,
  type Attendance,
  type AttendanceStatusEnumKey,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { RadioGroup, type RadioOption } from '@/components/ui/radio';
import { Spinner } from '@/components/ui/spinner';
import { View } from '@/components/ui/view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const STATUSES: AttendanceStatusEnumKey[] = [
  'PRESENT',
  'ABSENT_JUSTIFIED',
  'ABSENT_UNJUSTIFIED',
  'PERMISSION',
];

export default function AttendanceRollCallScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const queryClient = useQueryClient();
  const {
    classId: classIdParam,
    courseId: courseIdParam,
    sessionId: sessionIdParam,
  } = useLocalSearchParams<{
    classId: string;
    courseId: string;
    sessionId: string;
  }>();
  const classId = Number(classIdParam);
  const courseId = Number(courseIdParam);
  const sessionId = Number(sessionIdParam);

  // Only holds statuses the user actively picked in this session; anything else
  // falls back to the previously recorded attendance, then to PRESENT by default.
  const [overrides, setOverrides] = useState<Record<number, AttendanceStatusEnumKey>>({});
  const [formError, setFormError] = useState<string | null>(null);

  const {
    data: enrollmentsData,
    isLoading: isEnrollmentsLoading,
    isError: isEnrollmentsError,
    isSuccess: isEnrollmentsSuccess,
  } = useActiveCourseEnrollments(classId, courseId);
  const enrollments = enrollmentsData?.items ?? [];
  const students = enrollments.filter(
    (enrollment): enrollment is typeof enrollment & { studentId: number } =>
      Number.isSafeInteger(enrollment.studentId) && (enrollment.studentId ?? 0) > 0,
  );
  const hasInvalidRoster =
    students.length !== enrollments.length ||
    new Set(students.map((student) => student.studentId)).size !== students.length;

  const {
    data: existingAttendance,
    isLoading: isExistingLoading,
    isError: isExistingError,
    isSuccess: isExistingSuccess,
  } = useGetAttendance(sessionId);
  const existingByStudentId = useMemo(() => {
    const entries: Record<number, Attendance> = {};
    for (const attendance of existingAttendance ?? []) {
      if (attendance.studentId != null) {
        entries[attendance.studentId] = attendance;
      }
    }
    return entries;
  }, [existingAttendance]);

  const isLoading = isEnrollmentsLoading || isExistingLoading;
  const hasLoadError = isEnrollmentsError || isExistingError || hasInvalidRoster;
  const isReady = isEnrollmentsSuccess && isExistingSuccess && !hasLoadError;

  function getStatus(studentId: number): AttendanceStatusEnumKey {
    return overrides[studentId] ?? existingByStudentId[studentId]?.status ?? 'PRESENT';
  }

  function setStatus(studentId: number, status: AttendanceStatusEnumKey) {
    if (isPending) return;
    setOverrides((current) => ({ ...current, [studentId]: status }));
  }

  const summary: Record<AttendanceStatusEnumKey, number> = {
    PRESENT: 0,
    ABSENT_JUSTIFIED: 0,
    ABSENT_UNJUSTIFIED: 0,
    PERMISSION: 0,
  };
  for (const student of students) {
    summary[getStatus(student.studentId)] += 1;
  }

  const { mutateAsync, isPending } = useRecordBulkAttendance({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  async function onSubmit() {
    if (!isReady || students.length === 0 || isPending) return;
    setFormError(null);
    try {
      await mutateAsync({
        sessionId,
        data: {
          entries: students.map((student) => ({
            studentId: student.studentId,
            status: getStatus(student.studentId),
            reason: existingByStudentId[student.studentId]?.reason,
          })),
        },
      });
      await queryClient.invalidateQueries({ queryKey: getAttendanceQueryKey(sessionId) });
      await queryClient.invalidateQueries({
        queryKey: [{ url: '/api/v1/students/:studentId/attendance' }],
      });
      showToast(t('classes.attendance.rollCall.successToast'), 'success');
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  const statusOptions: RadioOption[] = STATUSES.map((status) => ({
    label: t(`classes.attendance.statusOptionsShort.${status}`),
    value: status,
  }));

  return (
    <>
      <Stack.Screen options={{ title: t('classes.attendance.rollCall.title') }} />
      <SettingsListScreen>
        {isLoading ? (
          <Spinner />
        ) : hasLoadError ? (
          <ThemedText type="small" themeColor="danger">
            {t('classes.attendance.rollCall.errorFallback')}
          </ThemedText>
        ) : !isReady ? (
          <Spinner />
        ) : students.length === 0 ? (
          <ThemedText type="small" themeColor="textSecondary">
            {t('classes.attendance.rollCall.noStudents')}
          </ThemedText>
        ) : (
          <>
            <View style={styles.summaryRow}>
              {STATUSES.map((status) => (
                <View key={status} style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
                  <ThemedText type="small" themeColor="textSecondary">
                    {t(`classes.attendance.statusOptionsShort.${status}`)}: {summary[status]}
                  </ThemedText>
                </View>
              ))}
            </View>

            <View style={styles.list}>
              {students.map((student) => (
                <SettingsCard key={student.studentId} style={styles.card}>
                  <ThemedText type="smallBold">{student.studentName}</ThemedText>
                  <RadioGroup
                    disabled={isPending}
                    options={statusOptions}
                    value={getStatus(student.studentId)}
                    onValueChange={(value) =>
                      setStatus(student.studentId, value as AttendanceStatusEnumKey)
                    }
                    orientation="horizontal"
                    style={styles.radioGroup}
                  />
                </SettingsCard>
              ))}
            </View>

            {formError && (
              <ThemedText type="small" themeColor="danger">
                {formError}
              </ThemedText>
            )}

            <SubmitButton
              label={t('classes.attendance.rollCall.submit')}
              onPress={onSubmit}
              isPending={isPending}
            />
          </>
        )}
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  summaryRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.two,
  },
  chip: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
  },
  list: {
    gap: Spacing.two,
  },
  card: {
    gap: Spacing.two,
  },
  radioGroup: {
    flexWrap: 'wrap',
    rowGap: Spacing.one,
  },
});
