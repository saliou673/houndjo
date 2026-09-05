import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter, type Href } from 'expo-router';
import { ClipboardList } from 'lucide-react-native';
import {
  useCancelSession,
  useGetCurrentUserPermissions,
  useGetSessions,
  type Session,
} from '@api-client';

import { DateField } from '@/components/date-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { ThemedText } from '@/components/themed-text';
import { AlertDialog } from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

const PAGE_SIZE = 25;

function SessionListItem({
  session,
  classId,
  courseId,
  canUpdate,
  canRecordProgress,
  onCancel,
}: {
  session: Session;
  classId: number;
  courseId: number;
  canUpdate: boolean;
  canRecordProgress: boolean;
  onCancel: () => void;
}) {
  const { t } = useTranslation();
  const theme = useTheme();
  const router = useRouter();
  const showRecordProgress = canRecordProgress && session.status !== 'CANCELLED';
  const showCancel = canUpdate && session.status === 'PLANNED';

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

      {(showRecordProgress || showCancel) && (
        <View style={styles.actionsRow}>
          {showRecordProgress && (
            <Button
              variant="outline"
              size="sm"
              icon={ClipboardList}
              onPress={() =>
                router.push({
                  pathname: '/classes/courses/sessions/progress',
                  params: {
                    classId: String(classId),
                    courseId: String(courseId),
                    sessionId: String(session.id),
                  },
                } as Href)
              }>
              {t('classes.sessions.recordProgressAction')}
            </Button>
          )}
          {showCancel && (
            <Button variant="outline" size="sm" onPress={onCancel}>
              {t('classes.sessions.cancelAction')}
            </Button>
          )}
        </View>
      )}
    </SettingsCard>
  );
}

export default function CourseSessionsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { classId: classIdParam, courseId: courseIdParam } = useLocalSearchParams<{
    classId: string;
    courseId: string;
  }>();
  const classId = Number(classIdParam);
  const courseId = Number(courseIdParam);

  const [page, setPage] = useState(0);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [cancelTarget, setCancelTarget] = useState<Session | null>(null);

  const { data: permissions, isLoading: isPermissionsLoading } = useGetCurrentUserPermissions();
  const canReadSessions = (permissions ?? []).some((permission) => permission.code === 'session:read');
  const canCreateSessions = (permissions ?? []).some((permission) => permission.code === 'session:create');
  const canUpdateSessions = (permissions ?? []).some((permission) => permission.code === 'session:update');
  const canRecordProgress = (permissions ?? []).some((permission) => permission.code === 'progress:create');

  const { data, isLoading, isError } = useGetSessions(
    courseId,
    {
      fromDate: fromDate || undefined,
      toDate: toDate || undefined,
      pageable: { page, size: PAGE_SIZE },
    },
    undefined,
    {
      query: {
        enabled: canReadSessions && Number.isFinite(courseId),
        placeholderData: (previous) => previous,
      },
    }
  );
  const rows = data?.items ?? [];
  const totalPages = data?.totalPages ?? 0;
  const canGoPrevious = page > 0;
  const canGoNext = page + 1 < totalPages;

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
        {isPermissionsLoading ? (
          <Spinner />
        ) : (
          <>
            {canCreateSessions && (
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
            )}

            {canReadSessions && (
              <>
                <View style={styles.filtersRow}>
                  <View style={styles.filterField}>
                    <DateField
                      label={t('classes.sessions.filters.fromDate')}
                      value={fromDate}
                      onChange={(value) => {
                        setFromDate(value);
                        setPage(0);
                      }}
                      placeholder="YYYY-MM-DD"
                      editable
                    />
                  </View>
                  <View style={styles.filterField}>
                    <DateField
                      label={t('classes.sessions.filters.toDate')}
                      value={toDate}
                      onChange={(value) => {
                        setToDate(value);
                        setPage(0);
                      }}
                      placeholder="YYYY-MM-DD"
                      editable
                    />
                  </View>
                </View>

                {isLoading ? (
                  <Spinner />
                ) : isError ? (
                  <ThemedText themeColor="danger" type="small">
                    {t('classes.sessions.errorFallback')}
                  </ThemedText>
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
                        classId={classId}
                        courseId={courseId}
                        canUpdate={canUpdateSessions}
                        canRecordProgress={canRecordProgress}
                        onCancel={() => setCancelTarget(session)}
                      />
                    ))}
                  </View>
                )}

                {!isLoading && !isError && rows.length > 0 && totalPages > 1 && (
                  <View style={styles.pager}>
                    <Button
                      variant="ghost"
                      size="sm"
                      disabled={!canGoPrevious}
                      onPress={() => setPage((current) => Math.max(0, current - 1))}>
                      {t('classes.sessions.previous')}
                    </Button>

                    <ThemedText type="small" themeColor="textSecondary">
                      {t('classes.sessions.pageIndicator', {
                        page: page + 1,
                        totalPages: Math.max(totalPages, 1),
                      })}
                    </ThemedText>

                    <Button
                      variant="ghost"
                      size="sm"
                      disabled={!canGoNext}
                      onPress={() => setPage((current) => current + 1)}>
                      {t('classes.sessions.next')}
                    </Button>
                  </View>
                )}
              </>
            )}
          </>
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
    gap: Spacing.two,
  },
  pager: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: Spacing.one,
  },
});
