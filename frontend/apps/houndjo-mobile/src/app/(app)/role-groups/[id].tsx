import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter, type Href } from 'expo-router';
import {
  getRoleGroupByIdAsAdminQueryKey,
  getRoleGroupsAsAdminQueryKey,
  useGetRoleGroupByIdAsAdmin,
  useUpdateRoleGroupAsAdmin,
  type RoleGroup,
} from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { extractApiErrorMessage } from '@/lib/api-error';

type FormValues = {
  name: string;
  description: string;
};

function mapRoleGroupToFormValues(roleGroup: RoleGroup): FormValues {
  return {
    name: roleGroup.name ?? '',
    description: roleGroup.description ?? '',
  };
}

export default function RoleGroupDetailScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const roleGroupId = Number(id);

  const {
    data: roleGroup,
    isLoading: isRoleGroupLoading,
    isError: isRoleGroupError,
  } = useGetRoleGroupByIdAsAdmin(roleGroupId);

  const [values, setValues] = useState<FormValues | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ name?: string }>({});
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (roleGroup) {
      setValues(mapRoleGroupToFormValues(roleGroup));
    }
  }, [roleGroup]);

  const { mutateAsync, isPending } = useUpdateRoleGroupAsAdmin({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => (current ? { ...current, [key]: next } : current));
  }

  async function onSubmit() {
    if (!values || !roleGroup) return;

    setFormError(null);

    const name = values.name.trim();
    if (!name) {
      setFieldErrors({ name: t('roleGroups.detail.validation.nameRequired') });
      return;
    }
    setFieldErrors({});

    try {
      const updatedRoleGroup = await mutateAsync({
        id: roleGroupId,
        data: {
          name,
          description: values.description.trim(),
          permissionCodes: (roleGroup.permissions ?? [])
            .map((permission) => permission.code)
            .filter((code): code is string => !!code),
        },
      });
      queryClient.setQueryData(getRoleGroupByIdAsAdminQueryKey(roleGroupId), updatedRoleGroup);
      await queryClient.invalidateQueries({
        queryKey: getRoleGroupsAsAdminQueryKey({ pageable: {} }),
      });
      showToast(t('roleGroups.detail.toastUpdated'), 'success');
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  const permissionCodes = (roleGroup?.permissions ?? [])
    .map((permission) => permission.code)
    .filter((code): code is string => !!code)
    .sort();

  return (
    <>
      <Stack.Screen options={{ title: roleGroup?.name || t('roleGroups.detail.title') }} />
      <SettingsListScreen>
        {isRoleGroupLoading || !values ? (
          <ActivityIndicator />
        ) : isRoleGroupError || !roleGroup ? (
          <ThemedText themeColor="danger">{t('roleGroups.detail.loadError')}</ThemedText>
        ) : (
          <>
            <SettingsCard>
              <FormTextField
                label={t('roleGroups.detail.fields.name')}
                value={values.name}
                onChangeText={(text) => updateField('name', text)}
                error={fieldErrors.name}
                editable={!isPending}
              />

              <FormTextField
                label={t('roleGroups.detail.fields.description')}
                value={values.description}
                onChangeText={(text) => updateField('description', text)}
                multiline
                editable={!isPending}
              />

              {formError && (
                <ThemedText type="small" themeColor="danger">
                  {formError}
                </ThemedText>
              )}

              <SubmitButton
                label={t('roleGroups.detail.submit')}
                onPress={() => void onSubmit()}
                isPending={isPending}
              />
            </SettingsCard>

            <SettingsCard>
              <ThemedText type="smallBold">{t('roleGroups.detail.permissionsLabel')}</ThemedText>
              {permissionCodes.length > 0 ? (
                <View style={styles.chipRow}>
                  {permissionCodes.map((code) => (
                    <View key={code} style={[styles.chip, { borderColor: theme.backgroundSelected }]}>
                      <ThemedText type="small" themeColor="textSecondary">
                        {code}
                      </ThemedText>
                    </View>
                  ))}
                </View>
              ) : (
                <ThemedText type="small" themeColor="textSecondary">
                  {t('roleGroups.detail.noPermissions')}
                </ThemedText>
              )}

              <Pressable
                accessibilityRole="button"
                onPress={() => router.push(`/role-groups/permissions/${roleGroupId}` as Href)}
                style={({ pressed }) => pressed && styles.pressed}>
                <ThemedText type="linkPrimary">{t('roleGroups.detail.managePermissions')}</ThemedText>
              </Pressable>
            </SettingsCard>
          </>
        )}
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
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
});
