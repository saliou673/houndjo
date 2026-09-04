import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import { useCreateSession } from '@api-client';

import { DateField } from '@/components/date-field';
import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';

export default function CreateSessionScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { courseId: courseIdParam } = useLocalSearchParams<{ courseId: string }>();
  const courseId = Number(courseIdParam);

  const [sessionDate, setSessionDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');

  const { mutate, isPending } = useCreateSession({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: [{ url: '/api/v1/courses/:courseId/sessions', params: { courseId } }],
        });
        router.back();
      },
    },
  });

  return (
    <>
      <Stack.Screen options={{ title: t('classes.sessions.formDialog.title') }} />
      <SettingsListScreen description={t('classes.sessions.formDialog.description')}>
        <SettingsCard>
          <DateField
            label={t('classes.sessions.formDialog.sessionDate')}
            value={sessionDate}
            onChange={setSessionDate}
            placeholder="YYYY-MM-DD"
            editable={!isPending}
          />

          <FormTextField
            label={t('classes.sessions.formDialog.startTime')}
            value={startTime}
            onChangeText={setStartTime}
            placeholder="HH:mm"
            editable={!isPending}
          />

          <FormTextField
            label={t('classes.sessions.formDialog.endTime')}
            value={endTime}
            onChangeText={setEndTime}
            placeholder="HH:mm"
            editable={!isPending}
          />

          <SubmitButton
            label={t('classes.sessions.formDialog.submit')}
            onPress={() => {
              if (!sessionDate.trim()) return;
              mutate({
                courseId,
                data: {
                  sessionDate,
                  startTime: startTime.trim() || undefined,
                  endTime: endTime.trim() || undefined,
                },
              });
            }}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
