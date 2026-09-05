import { useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter, type Href } from 'expo-router';
import {
  getEnrollmentsQueryKey,
  useEndEnrollment,
  useGetCurrentUserPermissions,
  useGetEnrollments,
  type Enrollment,
  type EnrollmentStatusEnumKey,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { AlertDialog } from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { Picker, type PickerOption } from '@/components/ui/picker';
import { Spinner } from '@/components/ui/spinner';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

const PAGE_SIZE = 20;
const STATUSES: EnrollmentStatusEnumKey[] = ['ACTIVE', 'ENDED'];

function EnrollmentListItem({
  enrollment,
  canUpdate,
  onManageCourses,
  onEnd,
}: {
  enrollment: Enrollment;
  canUpdate: boolean;
  onManageCourses: () => void;
  onEnd: () => void;
}) {
  const { t } = useTranslation();
  const theme = useTheme();

  return (
    <SettingsCard style={styles.card}>
      <View style={styles.headerRowItem}>
        <ThemedText type="smallBold" numberOfLines={1} style={styles.grow}>
          {enrollment.studentName}
        </ThemedText>
        <View style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
          <ThemedText type="small" themeColor="textSecondary">
            {t(`enrollments.list.statusOptions.${enrollment.status}`)}
          </ThemedText>
        </View>
      </View>

      <ThemedText type="small" themeColor="textSecondary">
        {enrollment.className} · {t('enrollments.list.courseCount', {
          count: enrollment.courseIds?.length ?? 0,
        })}
      </ThemedText>

      {canUpdate && (
        <View style={styles.actionsRow}>
          <Button variant="outline" size="sm" onPress={onManageCourses}>
            {t('enrollments.list.manageCoursesAction')}
          </Button>
          {enrollment.status === 'ACTIVE' && (
            <Button variant="outline" size="sm" onPress={onEnd}>
              {t('enrollments.list.endAction')}
            </Button>
          )}
        </View>
      )}
    </SettingsCard>
  );
}

export default function EnrollmentsListScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [page, setPage] = useState(0);
  const [status, setStatus] = useState<EnrollmentStatusEnumKey | undefined>(undefined);
  const [endTarget, setEndTarget] = useState<Enrollment | null>(null);

  const { data: permissions } = useGetCurrentUserPermissions();
  const canCreateEnrollments = (permissions ?? []).some(
    (permission) => permission.code === 'enrollment:create'
  );
  const canUpdateEnrollments = (permissions ?? []).some(
    (permission) => permission.code === 'enrollment:update'
  );

  const { data, isLoading, isFetching, isError, refetch } = useGetEnrollments(
    { status, pageable: { page, size: PAGE_SIZE } },
    undefined,
    { query: { placeholderData: (previous) => previous } }
  );

  const { mutate: endEnrollment } = useEndEnrollment({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: getEnrollmentsQueryKey({ pageable: {} }) });
        setEndTarget(null);
      },
    },
  });

  const items = data?.items ?? [];
  const totalPages = data?.totalPages ?? 0;
  const canGoPrevious = page > 0;
  const canGoNext = page + 1 < totalPages;

  const statusOptions: PickerOption[] = STATUSES.map((value) => ({
    label: t(`enrollments.list.statusOptions.${value}`),
    value,
  }));

  function handleRefresh() {
    if (page === 0) {
      void refetch();
      return;
    }
    setPage(0);
  }

  return (
    <>
      <Stack.Screen options={{ title: t('enrollments.list.title') }} />
      <ThemedView style={styles.container}>
        <SafeAreaView style={styles.container} edges={['bottom']}>
          <View style={styles.content}>
            <View style={styles.headerRow}>
              <ThemedText type="small" themeColor="textSecondary" style={styles.headerDescription}>
                {t('enrollments.list.description')}
              </ThemedText>
              {canCreateEnrollments && (
                <Button size="sm" onPress={() => router.push('/enrollments/create' as Href)}>
                  {t('enrollments.list.addEnrollment')}
                </Button>
              )}
            </View>

            <Picker
              label={t('enrollments.list.filterStatus')}
              placeholder={t('enrollments.list.filterStatusPlaceholder')}
              options={statusOptions}
              value={status}
              onValueChange={(value) => {
                setStatus(value as EnrollmentStatusEnumKey);
                setPage(0);
              }}
              rightComponent={
                status ? (
                  <Pressable
                    accessibilityRole="button"
                    onPress={() => {
                      setStatus(undefined);
                      setPage(0);
                    }}>
                    <ThemedText type="link">{t('common.clear')}</ThemedText>
                  </Pressable>
                ) : undefined
              }
            />

            {isError && (
              <ThemedText themeColor="danger" type="small">
                {t('enrollments.list.errorFallback')}
              </ThemedText>
            )}

            {isLoading ? (
              <Spinner style={styles.loadingIndicator} />
            ) : (
              <FlatList
                data={items}
                keyExtractor={(item) => String(item.id ?? 0)}
                renderItem={({ item }) => (
                  <EnrollmentListItem
                    enrollment={item}
                    canUpdate={canUpdateEnrollments}
                    onManageCourses={() => router.push(`/enrollments/${item.id ?? 0}/courses` as Href)}
                    onEnd={() => setEndTarget(item)}
                  />
                )}
                style={styles.list}
                contentContainerStyle={styles.listContent}
                ItemSeparatorComponent={() => <View style={styles.separator} />}
                refreshControl={<RefreshControl refreshing={isFetching} onRefresh={handleRefresh} />}
                ListEmptyComponent={
                  <ThemedText themeColor="textSecondary" style={styles.emptyText}>
                    {t('enrollments.list.noResults')}
                  </ThemedText>
                }
                ListFooterComponent={
                  items.length > 0 ? (
                    <View style={styles.pager}>
                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={!canGoPrevious}
                        onPress={() => setPage((current) => Math.max(0, current - 1))}>
                        {t('enrollments.list.previous')}
                      </Button>

                      <ThemedText type="small" themeColor="textSecondary">
                        {t('enrollments.list.pageIndicator', {
                          page: page + 1,
                          totalPages: Math.max(totalPages, 1),
                        })}
                      </ThemedText>

                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={!canGoNext}
                        onPress={() => setPage((current) => current + 1)}>
                        {t('enrollments.list.next')}
                      </Button>
                    </View>
                  ) : null
                }
              />
            )}
          </View>
        </SafeAreaView>
      </ThemedView>

      {endTarget && (
        <AlertDialog
          isVisible={!!endTarget}
          onClose={() => setEndTarget(null)}
          title={t('enrollments.endDialog.title')}
          description={t('enrollments.endDialog.confirmMessage', {
            name: endTarget.studentName,
            className: endTarget.className,
          })}
          confirmText={t('enrollments.endDialog.confirmButton')}
          cancelText={t('common.cancel')}
          onConfirm={() => {
            if (endTarget.id != null) endEnrollment({ id: endTarget.id });
          }}
        />
      )}
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    width: '100%',
    maxWidth: MaxContentWidth,
    alignSelf: 'center',
    paddingHorizontal: Spacing.four,
    paddingTop: Spacing.four,
    gap: Spacing.three,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.two,
  },
  headerDescription: {
    flex: 1,
  },
  loadingIndicator: {
    marginTop: Spacing.five,
  },
  list: {
    flex: 1,
  },
  listContent: {
    paddingBottom: BottomTabInset + Spacing.four,
  },
  separator: {
    height: Spacing.two,
  },
  emptyText: {
    textAlign: 'center',
    marginTop: Spacing.five,
  },
  card: {
    gap: Spacing.two,
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
    paddingTop: Spacing.three,
  },
});
