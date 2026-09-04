import { useTranslation } from 'react-i18next';
import { type StudentGenderEnumKey } from '@api-client';

import { DateField } from '@/components/date-field';
import { FormTextField } from '@/components/form-text-field';
import { Picker, type PickerOption } from '@/components/ui/picker';

const GENDERS: StudentGenderEnumKey[] = ['MALE', 'FEMALE'];

export type StudentFormValues = {
  firstName: string;
  lastName: string;
  birthDate: string;
  gender?: StudentGenderEnumKey;
  guardianName: string;
  guardianPhone: string;
};

export type StudentFormFieldErrors = Partial<Record<'firstName' | 'lastName', string>>;

export const INITIAL_STUDENT_FORM_VALUES: StudentFormValues = {
  firstName: '',
  lastName: '',
  birthDate: '',
  gender: undefined,
  guardianName: '',
  guardianPhone: '',
};

export function validateStudentForm(
  values: StudentFormValues,
  t: (key: string) => string
): StudentFormFieldErrors {
  const errors: StudentFormFieldErrors = {};

  if (!values.firstName.trim()) {
    errors.firstName = t('students.form.validation.firstNameRequired');
  }
  if (!values.lastName.trim()) {
    errors.lastName = t('students.form.validation.lastNameRequired');
  }

  return errors;
}

export function buildStudentRequestData(values: StudentFormValues) {
  return {
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    birthDate: values.birthDate.trim() || undefined,
    gender: values.gender,
    guardianName: values.guardianName.trim() || undefined,
    guardianPhone: values.guardianPhone.trim() || undefined,
  };
}

type StudentFormFieldsProps = {
  values: StudentFormValues;
  errors: StudentFormFieldErrors;
  disabled?: boolean;
  onChange: <K extends keyof StudentFormValues>(key: K, value: StudentFormValues[K]) => void;
};

export function StudentFormFields({ values, errors, disabled, onChange }: StudentFormFieldsProps) {
  const { t } = useTranslation();

  const genderOptions: PickerOption[] = GENDERS.map((value) => ({
    label: t(`students.form.genderOptions.${value}`),
    value,
  }));

  return (
    <>
      <FormTextField
        label={t('students.form.fields.firstName')}
        value={values.firstName}
        onChangeText={(text) => onChange('firstName', text)}
        error={errors.firstName}
        editable={!disabled}
      />

      <FormTextField
        label={t('students.form.fields.lastName')}
        value={values.lastName}
        onChangeText={(text) => onChange('lastName', text)}
        error={errors.lastName}
        editable={!disabled}
      />

      <DateField
        label={t('students.form.fields.birthDate')}
        value={values.birthDate}
        onChange={(text) => onChange('birthDate', text)}
        placeholder="YYYY-MM-DD"
        editable={!disabled}
        maximumDate={new Date()}
      />

      <Picker
        label={t('students.form.fields.gender')}
        placeholder={t('students.form.fields.genderPlaceholder')}
        options={genderOptions}
        value={values.gender}
        onValueChange={(value) => onChange('gender', value as StudentGenderEnumKey)}
        disabled={disabled}
      />

      <FormTextField
        label={t('students.form.fields.guardianName')}
        value={values.guardianName}
        onChangeText={(text) => onChange('guardianName', text)}
        editable={!disabled}
      />

      <FormTextField
        label={t('students.form.fields.guardianPhone')}
        value={values.guardianPhone}
        onChangeText={(text) => onChange('guardianPhone', text)}
        keyboardType="phone-pad"
        editable={!disabled}
      />
    </>
  );
}
