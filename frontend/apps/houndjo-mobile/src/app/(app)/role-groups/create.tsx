import { useMemo, useState } from 'react';
import { ActivityIndicator, StyleSheet, Switch, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import {
  getRoleGroupsAsAdminQueryKey,
  useCreateRoleGroupAsAdmin,
  useGetPermissionsAsAdmin,
} from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const PERMISSIONS_PAGE_SIZE = 1000;

type FormValues = {
  name: string;
  description: string;
  permissionCodes: string[];
};

type FieldErrors = {
  name?: string;
  description?: string;
  permissionCodes?: string;
};

const INITIAL_VALUES: FormValues = {
  name: '',
  description: '',
  permissionCodes: [],
};

export default function CreateRoleGroupScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [values, setValues] = useState<FormValues>(INITIAL_VALUES);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

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

  const { mutateAsync, isPending } = useCreateRoleGroupAsAdmin({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  function togglePermission(code: string, included: boolean) {
    setValues((current) => ({
      ...current,
      permissionCodes: included
        ? [...current.permissionCodes, code]
        : current.permissionCodes.filter((existing) => existing !== code),
    }));
  }

  function validate(current: FormValues): FieldErrors {
    const errors: FieldErrors = {};

    if (!current.name.trim()) {
      errors.name = t('roleGroups.create.validation.nameRequired');
    }
    if (!current.description.trim()) {
      errors.description = t('roleGroups.create.validation.descriptionRequired');
    }
    if (current.permissionCodes.length === 0) {
      errors.permissionCodes = t('roleGroups.create.validation.permissionRequired');
    }

    return errors;
  }

  async function onSubmit() {
    const errors = validate(values);
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      await mutateAsync({
        data: {
          name: values.name.trim(),
          description: values.description.trim(),
          permissionCodes: values.permissionCodes,
        },
      });
      await queryClient.invalidateQueries({
        queryKey: getRoleGroupsAsAdminQueryKey({ pageable: {} }),
      });
      showToast(t('roleGroups.create.toastCreated'), 'success');
      router.back();
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  return (
    <>
      <Stack.Screen options={{ title: t('roleGroups.create.title') }} />
      <SettingsListScreen description={t('roleGroups.create.description')}>
        <SettingsCard>
          <FormTextField
            label={t('roleGroups.create.fields.name')}
            value={values.name}
            onChangeText={(text) => updateField('name', text)}
            error={fieldErrors.name}
            editable={!isPending}
          />

          <FormTextField
            label={t('roleGroups.create.fields.description')}
            value={values.description}
            onChangeText={(text) => updateField('description', text)}
            error={fieldErrors.description}
            multiline
            editable={!isPending}
          />
        </SettingsCard>

        <SettingsCard>
          <ThemedText type="smallBold">{t('roleGroups.create.fields.permissions')}</ThemedText>

          {isPermissionsLoading ? (
            <ActivityIndicator />
          ) : permissionOptions.length === 0 ? (
            <ThemedText type="small" themeColor="textSecondary">
              {t('roleGroups.create.permissionsEmpty')}
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
                    value={values.permissionCodes.includes(permission.code)}
                    disabled={isPending}
                    onValueChange={(next) => togglePermission(permission.code, next)}
                  />
                </View>
              ))}
            </View>
          )}

          {fieldErrors.permissionCodes && (
            <ThemedText type="small" themeColor="danger">
              {fieldErrors.permissionCodes}
            </ThemedText>
          )}
        </SettingsCard>

        {formError && (
          <ThemedText type="small" themeColor="danger">
            {formError}
          </ThemedText>
        )}

        <SubmitButton
          label={t('roleGroups.create.submit')}
          onPress={() => void onSubmit()}
          isPending={isPending}
        />
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
