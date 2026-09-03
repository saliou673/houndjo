import { useEffect, useMemo, useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useTranslation } from 'react-i18next';
import { Stack, useRouter, type Href } from 'expo-router';
import { useGetCurrentUserPermissions, useGetRoleGroupsAsAdmin, type RoleGroup } from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/button';
import { SearchBar } from '@/components/ui/searchbar';
import { Spinner } from '@/components/ui/spinner';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

const PAGE_SIZE = 20;
const MAX_VISIBLE_PERMISSIONS = 3;

function RoleGroupListItem({ roleGroup, onPress }: { roleGroup: RoleGroup; onPress: () => void }) {
  const theme = useTheme();
  const permissionCodes = (roleGroup.permissions ?? [])
    .map((permission) => permission.code)
    .filter((code): code is string => !!code)
    .sort();
  const visiblePermissions = permissionCodes.slice(0, MAX_VISIBLE_PERMISSIONS);
  const overflowCount = permissionCodes.length - visiblePermissions.length;

  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => pressed && styles.pressed}>
      <SettingsCard style={styles.card}>
        <ThemedText type="smallBold" numberOfLines={1}>
          {roleGroup.name}
        </ThemedText>

        <ThemedText type="small" themeColor="textSecondary" numberOfLines={2}>
          {roleGroup.description || '—'}
        </ThemedText>

        {permissionCodes.length > 0 && (
          <View style={styles.chipRow}>
            {visiblePermissions.map((code) => (
              <View key={code} style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
                <ThemedText type="small" themeColor="textSecondary">
                  {code}
                </ThemedText>
              </View>
            ))}
            {overflowCount > 0 && (
              <View style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
                <ThemedText type="small" themeColor="textSecondary">
                  +{overflowCount}
                </ThemedText>
              </View>
            )}
          </View>
        )}
      </SettingsCard>
    </Pressable>
  );
}

export default function RoleGroupsListScreen() {
  const { t } = useTranslation();
  const router = useRouter();

  const [nameInput, setNameInput] = useState('');
  const [debouncedName, setDebouncedName] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedName(nameInput.trim().toLowerCase()), 300);
    return () => clearTimeout(timeout);
  }, [nameInput]);

  const { data: permissions } = useGetCurrentUserPermissions();
  const canManageRoleGroups = (permissions ?? []).some(
    (permission) => permission.code === 'role-group:manage'
  );

  const { data, isLoading, isFetching, isError, refetch } = useGetRoleGroupsAsAdmin(
    { pageable: { page, size: PAGE_SIZE } },
    undefined,
    { query: { placeholderData: (previous) => previous } }
  );

  const allItems = data?.items ?? [];
  const items = useMemo(
    () =>
      debouncedName
        ? allItems.filter((roleGroup) => (roleGroup.name ?? '').toLowerCase().includes(debouncedName))
        : allItems,
    [allItems, debouncedName]
  );
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
      <Stack.Screen options={{ title: t('roleGroups.list.title') }} />
      <ThemedView style={styles.container}>
        <SafeAreaView style={styles.container} edges={['bottom']}>
          <View style={styles.content}>
            <View style={styles.headerRow}>
              <ThemedText type="small" themeColor="textSecondary" style={styles.headerDescription}>
                {t('roleGroups.list.description')}
              </ThemedText>
              {canManageRoleGroups && (
                <Button size="sm" onPress={() => router.push('/role-groups/create' as Href)}>
                  {t('roleGroups.list.addRoleGroup')}
                </Button>
              )}
            </View>

            <SearchBar
              value={nameInput}
              onChangeText={setNameInput}
              placeholder={t('roleGroups.list.searchPlaceholder')}
              autoCapitalize="none"
              autoCorrect={false}
            />

            {isError && (
              <ThemedText themeColor="danger" type="small">
                {t('roleGroups.list.errorFallback')}
              </ThemedText>
            )}

            {isLoading ? (
              <Spinner style={styles.loadingIndicator} />
            ) : (
              <FlatList
                data={items}
                keyExtractor={(roleGroup) => String(roleGroup.id ?? 0)}
                renderItem={({ item }) => (
                  <RoleGroupListItem
                    roleGroup={item}
                    onPress={() => router.push(`/role-groups/${item.id ?? 0}` as Href)}
                  />
                )}
                style={styles.list}
                contentContainerStyle={styles.listContent}
                ItemSeparatorComponent={() => <View style={styles.separator} />}
                refreshControl={<RefreshControl refreshing={isFetching} onRefresh={handleRefresh} />}
                ListEmptyComponent={
                  <ThemedText themeColor="textSecondary" style={styles.emptyText}>
                    {t('roleGroups.list.noResults')}
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
                        {t('roleGroups.list.previous')}
                      </Button>

                      <ThemedText type="small" themeColor="textSecondary">
                        {t('roleGroups.list.pageIndicator', {
                          page: page + 1,
                          totalPages: Math.max(totalPages, 1),
                        })}
                      </ThemedText>

                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={!canGoNext}
                        onPress={() => setPage((current) => current + 1)}>
                        {t('roleGroups.list.next')}
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
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.one,
  },
  chip: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
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
