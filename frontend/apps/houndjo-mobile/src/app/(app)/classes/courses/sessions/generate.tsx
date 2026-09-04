import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import { useGenerateSessions } from '@api-client';

import { DateField } from '@/components/date-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { showToast } from '@/components/toast/toast-store';

export default function GenerateSessionsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { courseId: courseIdParam } = useLocalSearchParams<{ courseId: string }>();
  const courseId = Number(courseIdParam);

  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  const { mutate, isPending } = useGenerateSessions({
    mutation: {
      onSuccess: async (generated) => {
        await queryClient.invalidateQueries({
          queryKey: [{ url: '/api/v1/courses/:courseId/sessions', params: { courseId } }],
        });
        showToast(t('classes.sessions.generateDialog.successToast', { count: generated.length }), 'success');
        router.back();
      },
    },
  });

  return (
    <>
      <Stack.Screen options={{ title: t('classes.sessions.generateDialog.title') }} />
      <SettingsListScreen description={t('classes.sessions.generateDialog.description')}>
        <SettingsCard>
          <DateField
            label={t('classes.sessions.generateDialog.fromDate')}
            value={fromDate}
            onChange={setFromDate}
            placeholder="YYYY-MM-DD"
            editable={!isPending}
          />

          <DateField
            label={t('classes.sessions.generateDialog.toDate')}
            value={toDate}
            onChange={setToDate}
            placeholder="YYYY-MM-DD"
            editable={!isPending}
          />

          <SubmitButton
            label={t('classes.sessions.generateDialog.submit')}
            onPress={() => {
              if (!fromDate.trim() || !toDate.trim()) return;
              mutate({ courseId, data: { fromDate, toDate } });
            }}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
