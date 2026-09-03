import { useEffect, useState } from 'react';
import { Platform, Pressable, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { Stack } from 'expo-router';
import DateTimePicker, {
  DateTimePickerAndroid,
  type DateTimePickerEvent,
} from '@react-native-community/datetimepicker';
import {
  updateUserRequestGenderEnum,
  useGetUserDetails,
  useUpdateAccount,
  type UpdateAccountMutationRequest,
  type UpdateUserRequestGenderEnumKey,
  type UserSummary,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { FormTextField } from '@/components/form-text-field';
import { ThemedText } from '@/components/themed-text';
import { RadioGroup } from '@/components/ui/radio';
import { Spinner } from '@/components/ui/spinner';
import { showToast } from '@/components/toast/toast-store';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { extractApiErrorMessage } from '@/lib/api-error';

// Mirrors the backend's date-only ISO format for `birthDate`.
const BIRTH_DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

function parseDateInput(value: string): Date {
  const match = BIRTH_DATE_PATTERN.exec(value.trim());
  if (!match) return new Date();

  const [year, month, day] = value.trim().split('-').map(Number);
  return new Date(year, month - 1, day);
}

function formatDateValue(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

type FormValues = {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  birthDate: string;
  gender: UpdateUserRequestGenderEnumKey;
  address: string;
  imageUrl: string;
};

type FieldErrors = Partial<Record<keyof FormValues, string>>;

function mapUserToFormValues(user: UserSummary): FormValues {
  return {
    firstName: user.firstName,
    lastName: user.lastName,
    phoneNumber: user.phoneNumber ?? '',
    birthDate: user.birthDate,
    gender: user.gender,
    address: user.address ?? '',
    imageUrl: user.imageUrl ?? '',
  };
}

function toUpdatePayload(values: FormValues): UpdateAccountMutationRequest {
  return {
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    phoneNumber: values.phoneNumber.trim() || undefined,
    birthDate: values.birthDate.trim() || undefined,
    gender: values.gender,
    address: values.address.trim() || undefined,
    imageUrl: values.imageUrl.trim() || undefined,
  };
}

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

function DateField({
  label,
  value,
  onChange,
  error,
  editable,
  placeholder,
}: {
  label: string;
  value: string;
  onChange: (next: string) => void;
  error?: string;
  editable: boolean;
  placeholder: string;
}) {
  const theme = useTheme();

  // No web implementation ships in @react-native-community/datetimepicker
  // (it renders null with a console warning there), so this app's web
  // target keeps the plain text field instead of silently losing the
  // control.
  if (Platform.OS === 'web') {
    return (
      <FormTextField
        label={label}
        value={value}
        onChangeText={onChange}
        placeholder={placeholder}
        error={error}
        autoCapitalize="none"
        autoCorrect={false}
        editable={editable}
      />
    );
  }

  function handleChange(event: DateTimePickerEvent, selectedDate?: Date) {
    if (event.type === 'set' && selectedDate) {
      onChange(formatDateValue(selectedDate));
    }
  }

  if (Platform.OS === 'android') {
    return (
      <View style={styles.field}>
        <ThemedText type="smallBold">{label}</ThemedText>
        <Pressable
          accessibilityRole="button"
          disabled={!editable}
          onPress={() =>
            DateTimePickerAndroid.open({
              value: parseDateInput(value),
              mode: 'date',
              maximumDate: new Date(),
              onChange: handleChange,
            })
          }
          style={[
            styles.dateTrigger,
            {
              backgroundColor: theme.backgroundElement,
              borderColor: error ? theme.danger : theme.backgroundSelected,
            },
          ]}>
          <ThemedText themeColor={value ? 'text' : 'textSecondary'}>
            {value || placeholder}
          </ThemedText>
        </Pressable>
        {error && (
          <ThemedText type="small" themeColor="danger">
            {error}
          </ThemedText>
        )}
      </View>
    );
  }

  return (
    <View style={styles.field}>
      <ThemedText type="smallBold">{label}</ThemedText>
      <DateTimePicker
        value={parseDateInput(value)}
        mode="date"
        display="compact"
        maximumDate={new Date()}
        disabled={!editable}
        onChange={handleChange}
        style={styles.dateTriggerIOS}
      />
      {error && (
        <ThemedText type="small" themeColor="danger">
          {error}
        </ThemedText>
      )}
    </View>
  );
}

export default function ProfileScreen() {
  const { t } = useTranslation();
  const { data: user, isLoading, isError } = useGetUserDetails();

  const [values, setValues] = useState<FormValues | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useUpdateAccount({
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
      errors.firstName = t('settings.profile.firstNameRequired');
    }
    if (!current.lastName.trim()) {
      errors.lastName = t('settings.profile.lastNameRequired');
    }
    if (current.birthDate.trim() && !BIRTH_DATE_PATTERN.test(current.birthDate.trim())) {
      errors.birthDate = t('settings.profile.birthDateInvalid');
    }

    return errors;
  }

  async function onSubmit() {
    if (!values) return;

    const errors = validate(values);
    setFieldErrors(errors);
    setFormError(null);

    if (Object.values(errors).some(Boolean)) return;

    try {
      await mutateAsync({ data: toUpdatePayload(values) });
      showToast(t('settings.profile.toastUpdated'), 'success');
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
      <Stack.Screen options={{ title: t('settings.nav.profile') }} />
      <SettingsListScreen>
        {isLoading || !values ? (
          <Spinner />
        ) : isError || !user ? (
          <ThemedText themeColor="danger">{t('settings.profile.loadError')}</ThemedText>
        ) : (
          <SettingsCard>
            <ReadOnlyRow label={t('settings.profile.fields.email')} value={user.email} />

            <FormTextField
              label={t('settings.profile.fields.firstName')}
              value={values.firstName}
              onChangeText={(text) => updateField('firstName', text)}
              error={fieldErrors.firstName}
              editable={!isPending}
            />

            <FormTextField
              label={t('settings.profile.fields.lastName')}
              value={values.lastName}
              onChangeText={(text) => updateField('lastName', text)}
              error={fieldErrors.lastName}
              editable={!isPending}
            />

            <FormTextField
              label={t('settings.profile.fields.phoneNumber')}
              value={values.phoneNumber}
              onChangeText={(text) => updateField('phoneNumber', text)}
              keyboardType="phone-pad"
              editable={!isPending}
            />

            <DateField
              label={t('settings.profile.fields.birthDate')}
              value={values.birthDate}
              onChange={(text) => updateField('birthDate', text)}
              placeholder="YYYY-MM-DD"
              error={fieldErrors.birthDate}
              editable={!isPending}
            />

            <View style={styles.field}>
              <ThemedText type="smallBold">{t('settings.profile.fields.gender')}</ThemedText>
              <RadioGroup
                orientation="horizontal"
                value={values.gender}
                onValueChange={(next) =>
                  updateField('gender', next as UpdateUserRequestGenderEnumKey)
                }
                options={[
                  {
                    value: updateUserRequestGenderEnum.MALE,
                    label: t('settings.profile.fields.genderMale'),
                  },
                  {
                    value: updateUserRequestGenderEnum.FEMALE,
                    label: t('settings.profile.fields.genderFemale'),
                  },
                ]}
              />
            </View>

            <FormTextField
              label={t('settings.profile.fields.address')}
              value={values.address}
              onChangeText={(text) => updateField('address', text)}
              multiline
              editable={!isPending}
            />

            <FormTextField
              label={t('settings.profile.fields.imageUrl')}
              value={values.imageUrl}
              onChangeText={(text) => updateField('imageUrl', text)}
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isPending}
            />

            {formError && (
              <ThemedText type="small" themeColor="danger">
                {formError}
              </ThemedText>
            )}

            <SubmitButton
              label={t('settings.profile.submit')}
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
  readOnlyRow: {
    gap: Spacing.half,
  },
  dateTrigger: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    minHeight: 44,
    justifyContent: 'center',
  },
  dateTriggerIOS: {
    alignSelf: 'flex-start',
  },
});
