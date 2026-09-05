import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Stack, useLocalSearchParams } from 'expo-router';
import { useActiveCourseEnrollments, useGetCourseById } from '@api-client';

import { ProgressEntryCard } from '@/components/progress-entry-card';
import { ProgressStateCard } from '@/components/progress-state-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { ThemedText } from '@/components/themed-text';
import { Picker, type PickerOption } from '@/components/ui/picker';
import { Spinner } from '@/components/ui/spinner';

export default function RecordProgressScreen() {
  const { t } = useTranslation();
  const { classId: classIdParam, courseId: courseIdParam, sessionId: sessionIdParam } = useLocalSearchParams<{
    classId: string;
    courseId: string;
    sessionId: string;
  }>();
  const classId = Number(classIdParam);
  const courseId = Number(courseIdParam);
  const sessionId = Number(sessionIdParam);

  const [studentId, setStudentId] = useState<string | undefined>(undefined);

  const {
    data: course,
    isLoading: isCourseLoading,
    isError: isCourseError,
  } = useGetCourseById(classId, courseId);
  const { data: enrollmentsData, isLoading: isEnrollmentsLoading } = useActiveCourseEnrollments(
    classId,
    courseId
  );

  const students = (enrollmentsData?.items ?? []).filter(
    (enrollment) => enrollment.studentId != null
  );
  const studentOptions: PickerOption[] = students.map((enrollment) => ({
    label: enrollment.studentName ?? '',
    value: String(enrollment.studentId),
  }));

  const isLoading = isCourseLoading || isEnrollmentsLoading;
  const selectedStudentId = studentId ? Number(studentId) : undefined;

  return (
    <>
      <Stack.Screen options={{ title: t('classes.progress.entry.title') }} />
      <SettingsListScreen>
        {isLoading ? (
          <Spinner />
        ) : isCourseError || !course ? (
          <ThemedText type="small" themeColor="danger">
            {t('classes.progress.entry.errorFallback')}
          </ThemedText>
        ) : (
          <>
            {students.length === 0 ? (
              <ThemedText type="small" themeColor="textSecondary">
                {t('classes.progress.entry.noStudents')}
              </ThemedText>
            ) : (
              <Picker
                label={t('classes.progress.entry.studentLabel')}
                placeholder={t('classes.progress.entry.studentPlaceholder')}
                options={studentOptions}
                value={studentId}
                onValueChange={setStudentId}
                searchable
              />
            )}

            {selectedStudentId != null && (
              <>
                {course.type === 'QURAN' && (
                  <ProgressStateCard studentId={selectedStudentId} courseId={courseId} />
                )}
                <ProgressEntryCard
                  key={selectedStudentId}
                  courseId={courseId}
                  sessionId={sessionId}
                  studentId={selectedStudentId}
                  courseType={course.type ?? 'QAIDA'}
                  qaidaLessons={course.qaidaLessons ?? []}
                />
              </>
            )}
          </>
        )}
      </SettingsListScreen>
    </>
  );
}
