import { useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, StyleSheet, Switch, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import {
  getRoleGroupByIdAsAdminQueryKey,
  getRoleGroupsAsAdminQueryKey,
  useGetPermissionsAsAdmin,
  useGetRoleGroupByIdAsAdmin,
  useUpdateRoleGroupAsAdmin,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const PERMISSIONS_PAGE_SIZE = 1000;

export default function RoleGroupPermissionsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const roleGroupId = Number(id);

  const {
    data: roleGroup,
    isLoading: isRoleGroupLoading,
    isError: isRoleGroupError,
  } = useGetRoleGroupByIdAsAdmin(roleGroupId);
  const { data: permissionsData, isLoading: isPermissionsLoading } = useGetPermissionsAsAdmin({
    pageable: { page: 0, size: PERMISSIONS_PAGE_SIZE },
  });

  const permissionOptions = useMemo(
    () =>
      (permissionsData?.items ?? [])
        .filter((permission): permission is typeof permission & { code: string } => !!permission.code)
        .sort((a, b) => a.code.localeCompare(b.code)),
    [permissionsData?.items]
  );

  const [selectedCodes, setSelectedCodes] = useState<string[] | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (roleGroup) {
      setSelectedCodes(
        (roleGroup.permissions ?? [])
          .map((permission) => permission.code)
          .filter((code): code is string => !!code)
      );
    }
  }, [roleGroup]);

  const { mutateAsync, isPending } = useUpdateRoleGroupAsAdmin({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function togglePermission(code: string, included: boolean) {
    setSelectedCodes((current) => {
      if (!current) return current;
      return included ? [...current, code] : current.filter((existing) => existing !== code);
    });
  }

  async function onSubmit() {
    if (!selectedCodes || !roleGroup) return;

    setFormError(null);

    try {
      const updatedRoleGroup = await mutateAsync({
        id: roleGroupId,
        data: {
          name: roleGroup.name ?? '',
          description: roleGroup.description ?? '',
          permissionCodes: selectedCodes,
        },
      });
      queryClient.setQueryData(getRoleGroupByIdAsAdminQueryKey(roleGroupId), updatedRoleGroup);
      await queryClient.invalidateQueries({
        queryKey: getRoleGroupsAsAdminQueryKey({ pageable: {} }),
      });
      showToast(t('roleGroups.permissions.toastUpdated'), 'success');
      router.back();
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  const isLoading = isRoleGroupLoading || isPermissionsLoading || !selectedCodes;

  return (
    <>
      <Stack.Screen options={{ title: t('roleGroups.permissions.title') }} />
      <SettingsListScreen description={t('roleGroups.permissions.description')}>
        {isLoading ? (
          <ActivityIndicator />
        ) : isRoleGroupError || !roleGroup ? (
          <ThemedText themeColor="danger">{t('roleGroups.permissions.loadError')}</ThemedText>
        ) : (
          <SettingsCard>
            {permissionOptions.length === 0 ? (
              <ThemedText type="small" themeColor="textSecondary">
                {t('roleGroups.permissions.empty')}
              </ThemedText>
            ) : (
              <View style={styles.permissionList}>
                {permissionOptions.map((permission) => (
                  <View key={permission.code} style={styles.switchRow}>
                    <View style={styles.switchLabel}>
                      <ThemedText type="code">{permission.code}</ThemedText>
                      {permission.description && (
                        <ThemedText type="small" themeColor="textSecondary">
                          {permission.description}
                        </ThemedText>
                      )}
                    </View>
                    <Switch
                      value={selectedCodes.includes(permission.code)}
                      disabled={isPending}
                      onValueChange={(next) => togglePermission(permission.code, next)}
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
              label={t('roleGroups.permissions.submit')}
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
  permissionList: {
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
