import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter, type Href } from 'expo-router';
import { useCancelSession, useGetSessions, type Session } from '@api-client';

import { DateField } from '@/components/date-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { ThemedText } from '@/components/themed-text';
import { AlertDialog } from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

const PAGEABLE = { page: 0, size: 100 };

function SessionListItem({
  session,
  canUpdate,
  onCancel,
}: {
  session: Session;
  canUpdate: boolean;
  onCancel: () => void;
}) {
  const { t } = useTranslation();
  const theme = useTheme();

  return (
    <SettingsCard style={styles.card}>
      <View style={styles.headerRowItem}>
        <ThemedText type="smallBold" style={styles.grow}>
          {session.sessionDate}
        </ThemedText>
        <View style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
          <ThemedText type="small" themeColor="textSecondary">
            {t(`classes.sessions.statusOptions.${session.status}`)}
          </ThemedText>
        </View>
      </View>

      {session.startTime && session.endTime && (
        <ThemedText type="small" themeColor="textSecondary">
          {session.startTime}–{session.endTime}
        </ThemedText>
      )}

      {session.teacherName && (
        <ThemedText type="small" themeColor="textSecondary">
          {session.teacherName}
        </ThemedText>
      )}

      {canUpdate && session.status === 'PLANNED' && (
        <View style={styles.actionsRow}>
          <Button variant="outline" size="sm" onPress={onCancel}>
            {t('classes.sessions.cancelAction')}
          </Button>
        </View>
      )}
    </SettingsCard>
  );
}

export default function CourseSessionsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { courseId: courseIdParam } = useLocalSearchParams<{ courseId: string }>();
  const courseId = Number(courseIdParam);

  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [cancelTarget, setCancelTarget] = useState<Session | null>(null);

  const { data, isLoading, isError } = useGetSessions(courseId, {
    fromDate: fromDate || undefined,
    toDate: toDate || undefined,
    pageable: PAGEABLE,
  });
  const rows = data?.items ?? [];

  const { mutate: cancelSession } = useCancelSession({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: [{ url: '/api/v1/courses/:courseId/sessions', params: { courseId } }],
        });
        setCancelTarget(null);
      },
    },
  });

  return (
    <>
      <Stack.Screen options={{ title: t('classes.sessions.listTitle') }} />
      <SettingsListScreen>
        <View style={styles.headerRow}>
          <Button
            size="sm"
            onPress={() =>
              router.push({
                pathname: '/classes/courses/sessions/create',
                params: { courseId: String(courseId) },
              } as Href)
            }>
            {t('classes.sessions.addAction')}
          </Button>
          <Button
            size="sm"
            variant="outline"
            onPress={() =>
              router.push({
                pathname: '/classes/courses/sessions/generate',
                params: { courseId: String(courseId) },
              } as Href)
            }>
            {t('classes.sessions.generateAction')}
          </Button>
        </View>

        <View style={styles.filtersRow}>
          <View style={styles.filterField}>
            <DateField
              label={t('classes.sessions.filters.fromDate')}
              value={fromDate}
              onChange={setFromDate}
              placeholder="YYYY-MM-DD"
              editable
            />
          </View>
          <View style={styles.filterField}>
            <DateField
              label={t('classes.sessions.filters.toDate')}
              value={toDate}
              onChange={setToDate}
              placeholder="YYYY-MM-DD"
              editable
            />
          </View>
        </View>

        {isError && (
          <ThemedText themeColor="danger" type="small">
            {t('classes.sessions.errorFallback')}
          </ThemedText>
        )}

        {isLoading ? (
          <Spinner />
        ) : rows.length === 0 ? (
          <ThemedText type="small" themeColor="textSecondary">
            {t('classes.sessions.noResults')}
          </ThemedText>
        ) : (
          <View style={styles.list}>
            {rows.map((session) => (
              <SessionListItem
                key={session.id}
                session={session}
                canUpdate
                onCancel={() => setCancelTarget(session)}
              />
            ))}
          </View>
        )}
      </SettingsListScreen>

      {cancelTarget && (
        <AlertDialog
          isVisible={!!cancelTarget}
          onClose={() => setCancelTarget(null)}
          title={t('classes.sessions.cancelAction')}
          description={t('classes.sessions.cancelConfirmMessage', { date: cancelTarget.sessionDate })}
          confirmText={t('classes.sessions.cancelAction')}
          cancelText={t('common.cancel')}
          onConfirm={() => {
            if (cancelTarget.id != null) cancelSession({ courseId, id: cancelTarget.id });
          }}
        />
      )}
    </>
  );
}

const styles = StyleSheet.create({
  headerRow: {
    flexDirection: 'row',
    gap: Spacing.two,
  },
  filtersRow: {
    flexDirection: 'row',
    gap: Spacing.three,
  },
  filterField: {
    flex: 1,
  },
  list: {
    gap: Spacing.two,
  },
  card: {
    gap: Spacing.one,
  },
  headerRowItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.two,
  },
  grow: {
    flex: 1,
  },
  chip: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
  },
  actionsRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
});
