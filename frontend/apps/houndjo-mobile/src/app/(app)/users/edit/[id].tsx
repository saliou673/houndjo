import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import {
  getUserAsAdminQueryKey,
  getUsersAsAdminQueryKey,
  updateUserRequestGenderEnum,
  useGetUserAsAdmin,
  useUpdateUserAsAdmin,
  type UpdateUserRequestGenderEnumKey,
  type UserDetails,
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
  firstName: string;
  lastName: string;
  phoneNumber: string;
  birthDate: string;
  gender: UpdateUserRequestGenderEnumKey | undefined;
  address: string;
};

type FieldErrors = Partial<Record<keyof FormValues, string>>;

function mapUserToFormValues(user: UserDetails): FormValues {
  return {
    firstName: user.firstName,
    lastName: user.lastName,
    phoneNumber: user.phoneNumber ?? '',
    birthDate: user.birthDate ?? '',
    gender: user.gender,
    address: user.address ?? '',
  };
}

function GenderToggle({
  label,
  value,
  onChange,
  options,
}: {
  label: string;
  value: UpdateUserRequestGenderEnumKey | undefined;
  onChange: (next: UpdateUserRequestGenderEnumKey) => void;
  options: { value: UpdateUserRequestGenderEnumKey; label: string }[];
}) {
  const theme = useTheme();

  return (
    <View style={styles.field}>
      <ThemedText type="smallBold">{label}</ThemedText>
      <View style={styles.genderRow}>
        {options.map((option) => {
          const selected = option.value === value;
          return (
            <Pressable
              key={option.value}
              accessibilityRole="button"
              onPress={() => onChange(option.value)}
              style={[
                styles.genderOption,
                {
                  backgroundColor: selected ? theme.text : theme.backgroundElement,
                  borderColor: theme.backgroundSelected,
                },
              ]}>
              <ThemedText type="small" style={{ color: selected ? theme.background : theme.text }}>
                {option.label}
              </ThemedText>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

export default function EditUserScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { id } = useLocalSearchParams<{ id: string }>();
  const userId = Number(id);

  const { data: user, isLoading: isUserLoading, isError: isUserError } = useGetUserAsAdmin(userId);

  const [values, setValues] = useState<FormValues | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useUpdateUserAsAdmin({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  useEffect(() => {
    if (user) {
      setValues(mapUserToFormValues(user));
    }
  }, [user]);

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => (current ? { ...current, [key]: next } : current));
  }

  function validate(current: FormValues): FieldErrors {
    const errors: FieldErrors = {};

    if (!current.firstName.trim()) {
      errors.firstName = t('users.edit.validation.firstNameRequired');
    }
    if (!current.lastName.trim()) {
      errors.lastName = t('users.edit.validation.lastNameRequired');
    }

    return errors;
  }

  async function onSubmit() {
    if (!values || !Number.isFinite(userId) || userId <= 0) return;

    const errors = validate(values);
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      const updatedUser = await mutateAsync({
        id: userId,
        data: {
          firstName: values.firstName.trim(),
          lastName: values.lastName.trim(),
          phoneNumber: values.phoneNumber.trim() || undefined,
          birthDate: values.birthDate.trim() || undefined,
          gender: values.gender,
          address: values.address.trim() || undefined,
        },
      });
      queryClient.setQueryData(getUserAsAdminQueryKey(userId), updatedUser);
      await queryClient.invalidateQueries({
        queryKey: getUsersAsAdminQueryKey({ filter: {}, pageable: {} }),
      });
      showToast(t('users.edit.toastUpdated'), 'success');
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
      <Stack.Screen options={{ title: t('users.edit.title') }} />
      <SettingsListScreen>
        {isUserLoading || !values ? (
          <ActivityIndicator />
        ) : isUserError || !user ? (
          <ThemedText themeColor="danger">{t('users.edit.loadError')}</ThemedText>
        ) : (
          <SettingsCard>
            <FormTextField
              label={t('users.edit.fields.firstName')}
              value={values.firstName}
              onChangeText={(text) => updateField('firstName', text)}
              error={fieldErrors.firstName}
              editable={!isPending}
            />

            <FormTextField
              label={t('users.edit.fields.lastName')}
              value={values.lastName}
              onChangeText={(text) => updateField('lastName', text)}
              error={fieldErrors.lastName}
              editable={!isPending}
            />

            <FormTextField
              label={t('users.edit.fields.phoneNumber')}
              value={values.phoneNumber}
              onChangeText={(text) => updateField('phoneNumber', text)}
              keyboardType="phone-pad"
              editable={!isPending}
            />

            <FormTextField
              label={t('users.edit.fields.birthDate')}
              value={values.birthDate}
              onChangeText={(text) => updateField('birthDate', text)}
              placeholder="YYYY-MM-DD"
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isPending}
            />

            <GenderToggle
              label={t('users.edit.fields.gender')}
              value={values.gender}
              onChange={(next) => updateField('gender', next)}
              options={[
                {
                  value: updateUserRequestGenderEnum.MALE,
                  label: t('users.gender.MALE'),
                },
                {
                  value: updateUserRequestGenderEnum.FEMALE,
                  label: t('users.gender.FEMALE'),
                },
              ]}
            />

            <FormTextField
              label={t('users.edit.fields.address')}
              value={values.address}
              onChangeText={(text) => updateField('address', text)}
              multiline
              editable={!isPending}
            />

            {formError && (
              <ThemedText type="small" themeColor="danger">
                {formError}
              </ThemedText>
            )}

            <SubmitButton
              label={t('users.edit.submit')}
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
  field: {
    gap: Spacing.one,
  },
  genderRow: {
    flexDirection: 'row',
    gap: Spacing.two,
  },
  genderOption: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingVertical: Spacing.two,
  },
});
