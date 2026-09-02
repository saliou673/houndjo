import { ActivityIndicator, Alert, Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter, type Href } from 'expo-router';
import {
  getUserAsAdminQueryKey,
  getUsersAsAdminQueryKey,
  useDeleteUserAsAdmin,
  useGetUserAsAdmin,
  useGetUserPermissionsAsAdmin,
  userDetailsGenderEnum,
  userDetailsStatusEnum,
  type UserDetailsGenderEnumKey,
  type UserDetailsStatusEnumKey,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { extractApiErrorMessage } from '@/lib/api-error';

function ReadOnlyRow({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.readOnlyRow}>
      <ThemedText type="small" themeColor="textSecondary">
        {label}
      </ThemedText>
      <ThemedText>{value}</ThemedText>
    </View>
  );
}

function DeactivateButton({
  label,
  onPress,
  isPending,
}: {
  label: string;
  onPress: () => void;
  isPending: boolean;
}) {
  const theme = useTheme();

  return (
    <Pressable
      accessibilityRole="button"
      disabled={isPending}
      onPress={onPress}
      style={({ pressed }) => [
        styles.deactivateButton,
        { backgroundColor: theme.danger },
        (pressed || isPending) && styles.pressedButton,
      ]}>
      {isPending ? (
        <ActivityIndicator color={theme.background} />
      ) : (
        <ThemedText type="smallBold" style={{ color: theme.background }}>
          {label}
        </ThemedText>
      )}
    </Pressable>
  );
}

export default function UserDetailScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const userId = Number(id);

  const {
    data: user,
    isLoading: isUserLoading,
    isError: isUserError,
  } = useGetUserAsAdmin(userId);
  const { data: permissions, isLoading: isPermissionsLoading } =
    useGetUserPermissionsAsAdmin(userId);

  const { mutate: deactivateUser, isPending: isDeactivating } = useDeleteUserAsAdmin({
    mutation: {
      meta: { skipGlobalErrorToast: true },
      onSuccess: async () => {
        await Promise.all([
          queryClient.invalidateQueries({ queryKey: getUserAsAdminQueryKey(userId) }),
          queryClient.invalidateQueries({
            queryKey: getUsersAsAdminQueryKey({ filter: {}, pageable: {} }),
          }),
        ]);
        showToast(t('users.detail.deactivateSuccess'), 'success');
      },
      onError: (error) => {
        showToast(
          error instanceof AxiosError ? extractApiErrorMessage(error, t('errors.generic')) : t('errors.generic'),
          'error'
        );
      },
    },
  });

  function handleDeactivate() {
    if (!user) return;

    Alert.alert(
      t('users.detail.deactivateConfirmTitle'),
      t('users.detail.deactivateConfirmMessage', { email: user.email }),
      [
        { text: t('common.cancel'), style: 'cancel' },
        {
          text: t('users.detail.deactivate'),
          style: 'destructive',
          onPress: () => deactivateUser({ id: userId }),
        },
      ]
    );
  }

  const genderLabels: Record<UserDetailsGenderEnumKey, string> = {
    [userDetailsGenderEnum.MALE]: t('users.gender.MALE'),
    [userDetailsGenderEnum.FEMALE]: t('users.gender.FEMALE'),
  };
  const statusLabels: Record<UserDetailsStatusEnumKey, string> = {
    [userDetailsStatusEnum.NOT_ACTIVATED]: t('users.status.NOT_ACTIVATED'),
    [userDetailsStatusEnum.ACTIVATED]: t('users.status.ACTIVATED'),
    [userDetailsStatusEnum.DEACTIVATED]: t('users.status.DEACTIVATED'),
    [userDetailsStatusEnum.LOCKED]: t('users.status.LOCKED'),
    [userDetailsStatusEnum.BANNED]: t('users.status.BANNED'),
  };

  const fullName = user ? `${user.firstName} ${user.lastName}`.trim() : '';
  const roleGroupNames = (user?.roleGroups ?? [])
    .map((roleGroup) => roleGroup.name)
    .filter((name): name is string => !!name)
    .sort();
  const permissionList = (permissions ?? []).filter((permission) => !!permission.code);

  const isInvalidId = !Number.isFinite(userId) || userId <= 0;
  const isLoading = isUserLoading || isPermissionsLoading;
  const isError = isInvalidId || isUserError;

  return (
    <>
      <Stack.Screen options={{ title: fullName || t('users.detail.title') }} />
      <SettingsListScreen>
        {isLoading ? (
          <ActivityIndicator />
        ) : isError || !user ? (
          <ThemedText themeColor="danger">{t('users.detail.loadError')}</ThemedText>
        ) : (
          <>
            <SettingsCard>
              <ReadOnlyRow label={t('users.detail.fields.email')} value={user.email} />
              <ReadOnlyRow label={t('users.detail.fields.name')} value={fullName} />
              <ReadOnlyRow
                label={t('users.detail.fields.phoneNumber')}
                value={user.phoneNumber || '—'}
              />
              <ReadOnlyRow label={t('users.detail.fields.birthDate')} value={user.birthDate} />
              <ReadOnlyRow
                label={t('users.detail.fields.gender')}
                value={genderLabels[user.gender]}
              />
              <ReadOnlyRow label={t('users.detail.fields.address')} value={user.address || '—'} />
              <ReadOnlyRow
                label={t('users.detail.fields.status')}
                value={statusLabels[user.status ?? userDetailsStatusEnum.NOT_ACTIVATED]}
              />
            </SettingsCard>

            <SubmitButton
              label={t('users.detail.edit')}
              onPress={() => router.push(`/users/edit/${userId}` as Href)}
            />

            {user.status !== userDetailsStatusEnum.DEACTIVATED && (
              <DeactivateButton
                label={t('users.detail.deactivate')}
                onPress={handleDeactivate}
                isPending={isDeactivating}
              />
            )}

            <SettingsCard>
              <ThemedText type="smallBold">{t('users.detail.roleGroupsLabel')}</ThemedText>
              {roleGroupNames.length > 0 ? (
                <View style={styles.chipRow}>
                  {roleGroupNames.map((name) => (
                    <View key={name} style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
                      <ThemedText type="small" themeColor="textSecondary">
                        {name}
                      </ThemedText>
                    </View>
                  ))}
                </View>
              ) : (
                <ThemedText type="small" themeColor="textSecondary">
                  {t('users.detail.noRoleGroups')}
                </ThemedText>
              )}

              <Pressable
                accessibilityRole="button"
                onPress={() => router.push(`/users/role-groups/${userId}` as Href)}
                style={({ pressed }) => pressed && styles.pressed}>
                <ThemedText type="linkPrimary">{t('users.detail.manageRoleGroups')}</ThemedText>
              </Pressable>
            </SettingsCard>

            <SettingsCard>
              <ThemedText type="smallBold">{t('users.detail.permissionsLabel')}</ThemedText>
              {permissionList.length > 0 ? (
                <View style={styles.permissionList}>
                  {permissionList.map((permission) => (
                    <View key={permission.code} style={styles.permissionRow}>
                      <ThemedText type="small">{permission.code}</ThemedText>
                      {permission.description && (
                        <ThemedText type="small" themeColor="textSecondary">
                          {permission.description}
                        </ThemedText>
                      )}
                    </View>
                  ))}
                </View>
              ) : (
                <ThemedText type="small" themeColor="textSecondary">
                  {t('users.detail.noPermissions')}
                </ThemedText>
              )}
            </SettingsCard>
          </>
        )}
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  readOnlyRow: {
    gap: Spacing.half,
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
  permissionList: {
    gap: Spacing.two,
  },
  permissionRow: {
    gap: Spacing.half,
  },
  deactivateButton: {
    marginTop: Spacing.two,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: Spacing.two,
    paddingVertical: Spacing.three,
    minHeight: 48,
  },
  pressedButton: {
    opacity: 0.7,
  },
  pressed: {
    opacity: 0.7,
  },
});
