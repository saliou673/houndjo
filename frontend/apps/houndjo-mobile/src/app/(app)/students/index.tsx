import { useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useTranslation } from 'react-i18next';
import { Stack, useRouter, type Href } from 'expo-router';
import { useGetCurrentUserPermissions, useGetStudents, type Student } from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/button';
import { SearchBar } from '@/components/ui/searchbar';
import { Spinner } from '@/components/ui/spinner';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';

const PAGE_SIZE = 20;

function StudentListItem({ student, onPress }: { student: Student; onPress: () => void }) {
  const { t } = useTranslation();

  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => pressed && styles.pressed}>
      <SettingsCard style={styles.card}>
        <ThemedText type="smallBold" numberOfLines={1}>
          {student.firstName} {student.lastName}
        </ThemedText>

        {(student.guardianName || student.guardianPhone) && (
          <ThemedText type="small" themeColor="textSecondary" numberOfLines={1}>
            {[student.guardianName, student.guardianPhone].filter(Boolean).join(' · ')}
          </ThemedText>
        )}

        {!student.guardianName && !student.guardianPhone && student.birthDate && (
          <ThemedText type="small" themeColor="textSecondary">
            {t('students.list.birthDate', { date: student.birthDate })}
          </ThemedText>
        )}
      </SettingsCard>
    </Pressable>
  );
}

export default function StudentsListScreen() {
  const { t } = useTranslation();
  const router = useRouter();

  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');

  const { data: permissions } = useGetCurrentUserPermissions();
  const canCreateStudents = (permissions ?? []).some((permission) => permission.code === 'student:create');

  const { data, isLoading, isFetching, isError, refetch } = useGetStudents(
    { search: search.trim() || undefined, pageable: { page, size: PAGE_SIZE } },
    undefined,
    { query: { placeholderData: (previous) => previous } }
  );

  const items = data?.items ?? [];
  const totalPages = data?.totalPages ?? 0;
  const canGoPrevious = page > 0;
  const canGoNext = page + 1 < totalPages;

  function handleRefresh() {
    if (page === 0) {
      void refetch();
      return;
    }
    setPage(0);
  }

  return (
    <>
      <Stack.Screen options={{ title: t('students.list.title') }} />
      <ThemedView style={styles.container}>
        <SafeAreaView style={styles.container} edges={['bottom']}>
          <View style={styles.content}>
            <View style={styles.headerRow}>
              <ThemedText type="small" themeColor="textSecondary" style={styles.headerDescription}>
                {t('students.list.description')}
              </ThemedText>
              {canCreateStudents && (
                <Button size="sm" onPress={() => router.push('/students/create' as Href)}>
                  {t('students.list.addStudent')}
                </Button>
              )}
            </View>

            <SearchBar
              value={search}
              onChangeText={(text) => {
                setSearch(text);
                setPage(0);
              }}
              placeholder={t('students.list.searchPlaceholder')}
            />

            {isError && (
              <ThemedText themeColor="danger" type="small">
                {t('students.list.errorFallback')}
              </ThemedText>
            )}

            {isLoading ? (
              <Spinner style={styles.loadingIndicator} />
            ) : (
              <FlatList
                data={items}
                keyExtractor={(item) => String(item.id ?? 0)}
                renderItem={({ item }) => (
                  <StudentListItem
                    student={item}
                    onPress={() => router.push(`/students/${item.id ?? 0}` as Href)}
                  />
                )}
                style={styles.list}
                contentContainerStyle={styles.listContent}
                ItemSeparatorComponent={() => <View style={styles.separator} />}
                refreshControl={<RefreshControl refreshing={isFetching} onRefresh={handleRefresh} />}
                ListEmptyComponent={
                  <ThemedText themeColor="textSecondary" style={styles.emptyText}>
                    {t('students.list.noResults')}
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
                        {t('students.list.previous')}
                      </Button>

                      <ThemedText type="small" themeColor="textSecondary">
                        {t('students.list.pageIndicator', {
                          page: page + 1,
                          totalPages: Math.max(totalPages, 1),
                        })}
                      </ThemedText>

                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={!canGoNext}
                        onPress={() => setPage((current) => current + 1)}>
                        {t('students.list.next')}
                      </Button>
                    </View>
                  ) : null
                }
              />
            )}
          </View>
        </SafeAreaView>
      </ThemedView>
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
  pressed: {
    opacity: 0.7,
  },
  pager: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: Spacing.three,
  },
});
