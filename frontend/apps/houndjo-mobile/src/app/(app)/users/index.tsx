import { useEffect, useMemo, useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter, type Href } from 'expo-router';
import {
  getUsersAsAdminQueryKey,
  useDeleteUserAsAdmin,
  useGetUsersAsAdmin,
  userDetailsStatusEnum,
  type UserDetails,
  type UserDetailsStatusEnumKey,
  type UserFilter,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { showToast } from '@/components/toast/toast-store';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { AlertDialog } from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { SearchBar } from '@/components/ui/searchbar';
import { Spinner } from '@/components/ui/spinner';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const PAGE_SIZE = 20;

function UserListItem({
  user,
  statusLabel,
  onPress,
  onDeactivate,
}: {
  user: UserDetails;
  statusLabel: string;
  onPress: () => void;
  onDeactivate: () => void;
}) {
  const { t } = useTranslation();
  const theme = useTheme();
  const fullName = `${user.firstName} ${user.lastName}`.trim();
  const status = user.status ?? userDetailsStatusEnum.NOT_ACTIVATED;
  const isAlertStatus =
    status === userDetailsStatusEnum.LOCKED || status === userDetailsStatusEnum.BANNED;
  const roleGroupNames = (user.roleGroups ?? [])
    .map((roleGroup) => roleGroup.name)
    .filter((name): name is string => !!name)
    .sort();

  return (
    <SettingsCard style={styles.card}>
      <Pressable accessibilityRole="button" onPress={onPress} style={({ pressed }) => pressed && styles.pressed}>
        <View style={styles.cardHeader}>
          <ThemedText type="smallBold" style={styles.name} numberOfLines={1}>
            {fullName}
          </ThemedText>
          <View style={[styles.badge, { backgroundColor: theme.backgroundElement }]}>
            <ThemedText type="small" themeColor={isAlertStatus ? 'danger' : 'textSecondary'}>
              {statusLabel}
            </ThemedText>
          </View>
        </View>

        <ThemedText type="small" themeColor="textSecondary" numberOfLines={1}>
          {user.email}
        </ThemedText>

        {roleGroupNames.length > 0 && (
          <View style={styles.chipRow}>
            {roleGroupNames.map((name) => (
              <View key={name} style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
                <ThemedText type="small" themeColor="textSecondary">
                  {name}
                </ThemedText>
              </View>
            ))}
          </View>
        )}
      </Pressable>

      {status !== userDetailsStatusEnum.DEACTIVATED && (
        <Pressable
          accessibilityRole="button"
          onPress={onDeactivate}
          style={({ pressed }) => [styles.deactivateButton, pressed && styles.pressed]}>
          <ThemedText type="small" themeColor="danger">
            {t('users.list.deactivate')}
          </ThemedText>
        </Pressable>
      )}
    </SettingsCard>
  );
}

export default function UsersListScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [emailInput, setEmailInput] = useState('');
  const [debouncedEmail, setDebouncedEmail] = useState('');
  const [page, setPage] = useState(0);
  const [pendingDeactivation, setPendingDeactivation] = useState<UserDetails | null>(null);

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedEmail(emailInput.trim()), 300);
    return () => clearTimeout(timeout);
  }, [emailInput]);

  useEffect(() => {
    setPage(0);
  }, [debouncedEmail]);

  const filter = useMemo<UserFilter>(
    () => (debouncedEmail ? { email: { contains: debouncedEmail } } : {}),
    [debouncedEmail]
  );

  const { data, isLoading, isFetching, isError, refetch } = useGetUsersAsAdmin(
    { filter, pageable: { page, size: PAGE_SIZE } },
    undefined,
    { query: { placeholderData: (previous) => previous } }
  );

  const items = data?.items ?? [];
  const totalPages = data?.totalPages ?? 0;
  const canGoPrevious = page > 0;
  const canGoNext = page + 1 < totalPages;

  const statusLabels: Record<UserDetailsStatusEnumKey, string> = {
    [userDetailsStatusEnum.NOT_ACTIVATED]: t('users.status.NOT_ACTIVATED'),
    [userDetailsStatusEnum.ACTIVATED]: t('users.status.ACTIVATED'),
    [userDetailsStatusEnum.DEACTIVATED]: t('users.status.DEACTIVATED'),
    [userDetailsStatusEnum.LOCKED]: t('users.status.LOCKED'),
    [userDetailsStatusEnum.BANNED]: t('users.status.BANNED'),
  };

  function handleRefresh() {
    if (page === 0) {
      void refetch();
      return;
    }
    setPage(0);
  }

  const { mutate: deactivateUser } = useDeleteUserAsAdmin({
    mutation: {
      meta: { skipGlobalErrorToast: true },
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getUsersAsAdminQueryKey({ filter: {}, pageable: {} }),
        });
        showToast(t('users.list.deactivateSuccess'), 'success');
      },
      onError: (error) => {
        showToast(
          error instanceof AxiosError ? extractApiErrorMessage(error, t('errors.generic')) : t('errors.generic'),
          'error'
        );
      },
    },
  });

  function handleDeactivate(user: UserDetails) {
    setPendingDeactivation(user);
  }

  function confirmDeactivate() {
    if (pendingDeactivation) {
      deactivateUser({ id: pendingDeactivation.id ?? 0 });
    }
  }

  return (
    <>
      <Stack.Screen options={{ title: t('users.list.title') }} />
      <ThemedView style={styles.container}>
        <SafeAreaView style={styles.container} edges={['bottom']}>
          <View style={styles.content}>
            <View style={styles.headerRow}>
              <ThemedText type="small" themeColor="textSecondary" style={styles.headerDescription}>
                {t('users.list.description')}
              </ThemedText>
              <Button
                size="sm"
                onPress={() => router.push('/users/create' as Href)}>
                {t('users.list.addUser')}
              </Button>
            </View>

            <SearchBar
              value={emailInput}
              onChangeText={setEmailInput}
              placeholder={t('users.list.searchPlaceholder')}
              autoCapitalize="none"
              autoCorrect={false}
            />

            {isError && (
              <ThemedText themeColor="danger" type="small">
                {t('users.list.errorFallback')}
              </ThemedText>
            )}

            {isLoading ? (
              <Spinner style={styles.loadingIndicator} />
            ) : (
              <FlatList
                data={items}
                keyExtractor={(user) => String(user.id ?? 0)}
                renderItem={({ item }) => (
                  <UserListItem
                    user={item}
                    statusLabel={statusLabels[item.status ?? userDetailsStatusEnum.NOT_ACTIVATED]}
                    onPress={() => router.push(`/users/${item.id ?? 0}` as Href)}
                    onDeactivate={() => handleDeactivate(item)}
                  />
                )}
                style={styles.list}
                contentContainerStyle={styles.listContent}
                ItemSeparatorComponent={() => <View style={styles.separator} />}
                refreshControl={<RefreshControl refreshing={isFetching} onRefresh={handleRefresh} />}
                ListEmptyComponent={
                  <ThemedText themeColor="textSecondary" style={styles.emptyText}>
                    {t('users.list.noResults')}
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
                        {t('users.list.previous')}
                      </Button>

                      <ThemedText type="small" themeColor="textSecondary">
                        {t('users.list.pageIndicator', {
                          page: page + 1,
                          totalPages: Math.max(totalPages, 1),
                        })}
                      </ThemedText>

                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={!canGoNext}
                        onPress={() => setPage((current) => current + 1)}>
                        {t('users.list.next')}
                      </Button>
                    </View>
                  ) : null
                }
              />
            )}
          </View>
        </SafeAreaView>
      </ThemedView>

      <AlertDialog
        isVisible={!!pendingDeactivation}
        onClose={() => setPendingDeactivation(null)}
        title={t('users.list.deactivateConfirmTitle')}
        description={
          pendingDeactivation
            ? t('users.list.deactivateConfirmMessage', { email: pendingDeactivation.email })
            : undefined
        }
        confirmText={t('users.list.deactivate')}
        cancelText={t('common.cancel')}
        onConfirm={confirmDeactivate}
      />
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
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.two,
  },
  name: {
    flex: 1,
  },
  badge: {
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
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
  deactivateButton: {
    alignSelf: 'flex-start',
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
