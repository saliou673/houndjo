import { useMemo, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useRouter } from 'expo-router';
import {
  createAdminUserRequestGenderEnum,
  getUsersAsAdminQueryKey,
  useCreateUserAsAdmin,
  useGetRoleGroupsAsAdmin,
  type CreateAdminUserRequestGenderEnumKey,
} from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { RadioGroup } from '@/components/ui/radio';
import { Spinner } from '@/components/ui/spinner';
import { Switch } from '@/components/ui/switch';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const ROLE_GROUPS_PAGE_SIZE = 100;

type FormValues = {
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  birthDate: string;
  gender: CreateAdminUserRequestGenderEnumKey | undefined;
  address: string;
  roleGroupNames: string[];
};

type FieldErrors = Partial<Record<keyof FormValues, string>>;

const INITIAL_VALUES: FormValues = {
  email: '',
  firstName: '',
  lastName: '',
  phoneNumber: '',
  birthDate: '',
  gender: undefined,
  address: '',
  roleGroupNames: [],
};

export default function CreateUserScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();

  const [values, setValues] = useState<FormValues>(INITIAL_VALUES);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { data: roleGroupsData, isLoading: isRoleGroupsLoading } = useGetRoleGroupsAsAdmin({
    pageable: { page: 0, size: ROLE_GROUPS_PAGE_SIZE },
  });
  const roleGroupOptions = useMemo(
    () =>
      (roleGroupsData?.items ?? [])
        .filter((roleGroup): roleGroup is typeof roleGroup & { name: string } => !!roleGroup.name)
        .sort((a, b) => a.name.localeCompare(b.name)),
    [roleGroupsData?.items]
  );

  const { mutateAsync, isPending } = useCreateUserAsAdmin({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  function toggleRoleGroup(name: string, included: boolean) {
    setValues((current) => ({
      ...current,
      roleGroupNames: included
        ? [...current.roleGroupNames, name]
        : current.roleGroupNames.filter((existing) => existing !== name),
    }));
  }

  function validate(current: FormValues): FieldErrors {
    const errors: FieldErrors = {};

    if (!current.email.trim()) {
      errors.email = t('users.create.validation.emailRequired');
    } else if (!EMAIL_PATTERN.test(current.email.trim())) {
      errors.email = t('users.create.validation.emailInvalid');
    }

    if (!current.firstName.trim()) {
      errors.firstName = t('users.create.validation.firstNameRequired');
    }

    if (!current.lastName.trim()) {
      errors.lastName = t('users.create.validation.lastNameRequired');
    }

    if (current.roleGroupNames.length === 0) {
      errors.roleGroupNames = t('users.create.validation.roleGroupRequired');
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
          email: values.email.trim(),
          firstName: values.firstName.trim(),
          lastName: values.lastName.trim(),
          birthDate: values.birthDate.trim() || undefined,
          gender: values.gender,
          phoneNumber: values.phoneNumber.trim() || undefined,
          address: values.address.trim() || undefined,
          roleGroupNames: values.roleGroupNames,
        },
      });
      await queryClient.invalidateQueries({
        queryKey: getUsersAsAdminQueryKey({ filter: {}, pageable: {} }),
      });
      showToast(t('users.create.toastCreated'), 'success');
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
      <Stack.Screen options={{ title: t('users.create.title') }} />
      <SettingsListScreen description={t('users.create.description')}>
        <SettingsCard>
          <FormTextField
            label={t('users.create.fields.email')}
            value={values.email}
            onChangeText={(text) => updateField('email', text)}
            error={fieldErrors.email}
            autoCapitalize="none"
            autoCorrect={false}
            keyboardType="email-address"
            editable={!isPending}
          />

          <FormTextField
            label={t('users.create.fields.firstName')}
            value={values.firstName}
            onChangeText={(text) => updateField('firstName', text)}
            error={fieldErrors.firstName}
            editable={!isPending}
          />

          <FormTextField
            label={t('users.create.fields.lastName')}
            value={values.lastName}
            onChangeText={(text) => updateField('lastName', text)}
            error={fieldErrors.lastName}
            editable={!isPending}
          />

          <FormTextField
            label={t('users.create.fields.phoneNumber')}
            value={values.phoneNumber}
            onChangeText={(text) => updateField('phoneNumber', text)}
            keyboardType="phone-pad"
            editable={!isPending}
          />

          <FormTextField
            label={t('users.create.fields.birthDate')}
            value={values.birthDate}
            onChangeText={(text) => updateField('birthDate', text)}
            placeholder="YYYY-MM-DD"
            autoCapitalize="none"
            autoCorrect={false}
            editable={!isPending}
          />

          <View style={styles.field}>
            <ThemedText type="smallBold">{t('users.create.fields.gender')}</ThemedText>
            <RadioGroup
              orientation="horizontal"
              value={values.gender}
              onValueChange={(next) =>
                updateField('gender', next as CreateAdminUserRequestGenderEnumKey)
              }
              options={[
                {
                  value: createAdminUserRequestGenderEnum.MALE,
                  label: t('users.gender.MALE'),
                },
                {
                  value: createAdminUserRequestGenderEnum.FEMALE,
                  label: t('users.gender.FEMALE'),
                },
              ]}
            />
          </View>

          <FormTextField
            label={t('users.create.fields.address')}
            value={values.address}
            onChangeText={(text) => updateField('address', text)}
            multiline
            editable={!isPending}
          />
        </SettingsCard>

        <SettingsCard>
          <ThemedText type="smallBold">{t('users.create.fields.roleGroups')}</ThemedText>

          {isRoleGroupsLoading ? (
            <Spinner />
          ) : roleGroupOptions.length === 0 ? (
            <ThemedText type="small" themeColor="textSecondary">
              {t('users.create.roleGroupsEmpty')}
            </ThemedText>
          ) : (
            <View style={styles.roleGroupList}>
              {roleGroupOptions.map((roleGroup) => (
                <View key={roleGroup.name} style={styles.switchRow}>
                  <View style={styles.switchLabel}>
                    <ThemedText>{roleGroup.name}</ThemedText>
                    {roleGroup.description && (
                      <ThemedText type="small" themeColor="textSecondary">
                        {roleGroup.description}
                      </ThemedText>
                    )}
                  </View>
                  <Switch
                    value={values.roleGroupNames.includes(roleGroup.name)}
                    disabled={isPending}
                    onValueChange={(next) => toggleRoleGroup(roleGroup.name, next)}
                  />
                </View>
              ))}
            </View>
          )}

          {fieldErrors.roleGroupNames && (
            <ThemedText type="small" themeColor="danger">
              {fieldErrors.roleGroupNames}
            </ThemedText>
          )}
        </SettingsCard>

        {formError && (
          <ThemedText type="small" themeColor="danger">
            {formError}
          </ThemedText>
        )}

        <SubmitButton
          label={t('users.create.submit')}
          onPress={() => void onSubmit()}
          isPending={isPending}
        />
      </SettingsListScreen>
    </>
  );
}

const styles = StyleSheet.create({
  field: {
    gap: Spacing.one,
  },
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
