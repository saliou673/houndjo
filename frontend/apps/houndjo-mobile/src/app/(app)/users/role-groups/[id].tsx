import { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import {
  getUserAsAdminQueryKey,
  getUsersAsAdminQueryKey,
  useAssignRoleGroupAsAdmin,
  useGetRoleGroupsAsAdmin,
  useGetUserAsAdmin,
  useRevokeRoleGroupAsAdmin,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { Spinner } from '@/components/ui/spinner';
import { Switch } from '@/components/ui/switch';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const ROLE_GROUPS_PAGE_SIZE = 100;

export default function UserRoleGroupsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const userId = Number(id);

  const {
    data: user,
    isLoading: isUserLoading,
    isError: isUserError,
  } = useGetUserAsAdmin(userId);
  const { data: roleGroupsData, isLoading: isRoleGroupsLoading } = useGetRoleGroupsAsAdmin({
    pageable: { page: 0, size: ROLE_GROUPS_PAGE_SIZE },
  });

  const roleGroupOptions = useMemo(
    () =>
      (roleGroupsData?.items ?? [])
        .filter(
          (roleGroup): roleGroup is typeof roleGroup & { id: number; name: string } =>
            !!roleGroup.id && !!roleGroup.name
        )
        .sort((a, b) => a.name.localeCompare(b.name)),
    [roleGroupsData?.items]
  );

  const [selectedIds, setSelectedIds] = useState<number[] | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (user) {
      setSelectedIds(
        (user.roleGroups ?? [])
          .map((roleGroup) => roleGroup.id)
          .filter((roleGroupId): roleGroupId is number => !!roleGroupId)
      );
    }
  }, [user]);

  const { mutateAsync: assignRoleGroupAsync, isPending: isAssigning } = useAssignRoleGroupAsAdmin();
  const { mutateAsync: revokeRoleGroupAsync, isPending: isRevoking } = useRevokeRoleGroupAsAdmin();
  const isPending = isAssigning || isRevoking;

  function toggleRoleGroup(roleGroupId: number, included: boolean) {
    setSelectedIds((current) => {
      if (!current) return current;
      return included
        ? [...current, roleGroupId]
        : current.filter((existing) => existing !== roleGroupId);
    });
  }

  async function onSubmit() {
    if (!selectedIds) return;

    setFormError(null);

    const initialIds = new Set(
      (user?.roleGroups ?? [])
        .map((roleGroup) => roleGroup.id)
        .filter((roleGroupId): roleGroupId is number => !!roleGroupId)
    );
    const targetIds = new Set(selectedIds);
    const toAssign = [...targetIds].filter((roleGroupId) => !initialIds.has(roleGroupId));
    const toRevoke = [...initialIds].filter((roleGroupId) => !targetIds.has(roleGroupId));

    try {
      await Promise.all([
        ...toAssign.map((roleGroupId) =>
          assignRoleGroupAsync({ id: userId, data: { roleGroupId } })
        ),
        ...toRevoke.map((roleGroupId) => revokeRoleGroupAsync({ id: userId, roleGroupId })),
      ]);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: getUserAsAdminQueryKey(userId) }),
        queryClient.invalidateQueries({
          queryKey: getUsersAsAdminQueryKey({ filter: {}, pageable: {} }),
        }),
      ]);
      showToast(t('users.roleGroups.toastUpdated'), 'success');
      router.back();
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  const isLoading = isUserLoading || isRoleGroupsLoading || !selectedIds;

  return (
    <>
      <Stack.Screen options={{ title: t('users.roleGroups.title') }} />
      <SettingsListScreen description={t('users.roleGroups.description')}>
        {isLoading ? (
          <Spinner />
        ) : isUserError || !user ? (
          <ThemedText themeColor="danger">{t('users.roleGroups.loadError')}</ThemedText>
        ) : (
          <SettingsCard>
            {roleGroupOptions.length === 0 ? (
              <ThemedText type="small" themeColor="textSecondary">
                {t('users.roleGroups.empty')}
              </ThemedText>
            ) : (
              <View style={styles.roleGroupList}>
                {roleGroupOptions.map((roleGroup) => (
                  <View key={roleGroup.id} style={styles.switchRow}>
                    <View style={styles.switchLabel}>
                      <ThemedText>{roleGroup.name}</ThemedText>
                      {roleGroup.description && (
                        <ThemedText type="small" themeColor="textSecondary">
                          {roleGroup.description}
                        </ThemedText>
                      )}
                    </View>
                    <Switch
                      value={selectedIds.includes(roleGroup.id)}
                      disabled={isPending}
                      onValueChange={(next) => toggleRoleGroup(roleGroup.id, next)}
                    />
                  </View>
                ))}
              </View>
            )}

            {formError && (
              <ThemedText type="small" themeColor="danger">
                {formError}
              </ThemedText>
            )}

            <SubmitButton
              label={t('users.roleGroups.submit')}
              onPress={() => void onSubmit()}
              isPending={isPending}
            />
          </SettingsCard>
        )}
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  roleGroupList: {
    gap: Spacing.three,
  },
  switchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.three,
  },
  switchLabel: {
    flex: 1,
    gap: Spacing.half,
  },
});
