import { useEffect, useState, type ReactNode } from 'react';
import { StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import {
  getPaceQueryKey,
  useGetPace,
  useSetPace,
  type CourseTypeEnumKey,
  type PaceUnitEnumKey,
} from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { Picker, type PickerOption } from '@/components/ui/picker';
import { Spinner } from '@/components/ui/spinner';
import { Spacing } from '@/constants/theme';

const FLOW_UNITS: PaceUnitEnumKey[] = ['PAGE', 'VERSE', 'HIZB', 'NISF_HIZB'];
const BASE_UNITS_BY_COURSE_TYPE: Record<CourseTypeEnumKey, PaceUnitEnumKey[]> = {
  QURAN: FLOW_UNITS,
  QAIDA: ['LESSON'],
  BOOK: ['PAGE', 'CHAPTER'],
};

type PaceFormValues = {
  unit: PaceUnitEnumKey;
  amountPerSession: string;
  sessionsPerWeek: string;
  sabakUnit?: PaceUnitEnumKey;
  sabakAmount: string;
  sabqiUnit?: PaceUnitEnumKey;
  sabqiAmount: string;
  dhorUnit?: PaceUnitEnumKey;
  dhorAmount: string;
  dhorCycleDays: string;
};

function createInitialValues(courseType: CourseTypeEnumKey): PaceFormValues {
  return {
    unit: courseType === 'QAIDA' ? 'LESSON' : 'PAGE',
    amountPerSession: '1',
    sessionsPerWeek: '1',
    sabakUnit: undefined,
    sabakAmount: '',
    sabqiUnit: undefined,
    sabqiAmount: '',
    dhorUnit: undefined,
    dhorAmount: '',
    dhorCycleDays: '',
  };
}

type FieldErrors = Partial<
  Record<'sabakUnit' | 'sabakAmount' | 'sabqiUnit' | 'sabqiAmount' | 'dhorUnit' | 'dhorAmount' | 'dhorCycleDays', string>
>;

type FlowFieldsProps = {
  title: string;
  unitValue?: PaceUnitEnumKey;
  amountValue: string;
  unitError?: string;
  amountError?: string;
  disabled: boolean;
  onUnitChange: (value: PaceUnitEnumKey) => void;
  onAmountChange: (value: string) => void;
  unitOptions: PickerOption[];
  t: ReturnType<typeof useTranslation>['t'];
  children?: ReactNode;
};

function FlowFields({
  title,
  unitValue,
  amountValue,
  unitError,
  amountError,
  disabled,
  onUnitChange,
  onAmountChange,
  unitOptions,
  t,
  children,
}: FlowFieldsProps) {
  return (
    <View style={styles.flowSection}>
      <ThemedText type="smallBold">{title}</ThemedText>
      <Picker
        label={t('classes.pace.fields.unit')}
        placeholder={t('classes.pace.fields.unitPlaceholder')}
        options={unitOptions}
        value={unitValue}
        onValueChange={(value) => onUnitChange(value as PaceUnitEnumKey)}
        disabled={disabled}
        error={unitError}
      />
      <FormTextField
        label={t('classes.pace.fields.amount')}
        value={amountValue}
        onChangeText={onAmountChange}
        keyboardType="decimal-pad"
        error={amountError}
        editable={!disabled}
      />
      {children}
    </View>
  );
}

type CoursePaceCardProps = {
  courseId: number;
  courseType: CourseTypeEnumKey;
  canUpdate: boolean;
};

export function CoursePaceCard({ courseId, courseType, canUpdate }: CoursePaceCardProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const isQuran = courseType === 'QURAN';
  const baseUnits = BASE_UNITS_BY_COURSE_TYPE[courseType];
  const defaultUnit = courseType === 'QAIDA' ? 'LESSON' : 'PAGE';

  const { data: pace, isLoading } = useGetPace(courseId);
  const [values, setValues] = useState<PaceFormValues>(() => createInitialValues(courseType));
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  useEffect(() => {
    if (pace) {
      setValues({
        unit: pace.unit && baseUnits.includes(pace.unit) ? pace.unit : defaultUnit,
        amountPerSession: pace.amountPerSession != null ? String(pace.amountPerSession) : '1',
        sessionsPerWeek: pace.sessionsPerWeek != null ? String(pace.sessionsPerWeek) : '1',
        sabakUnit: pace.sabak?.unit,
        sabakAmount: pace.sabak?.amount != null ? String(pace.sabak.amount) : '',
        sabqiUnit: pace.sabqi?.unit,
        sabqiAmount: pace.sabqi?.amount != null ? String(pace.sabqi.amount) : '',
        dhorUnit: pace.dhor?.unit,
        dhorAmount: pace.dhor?.amount != null ? String(pace.dhor.amount) : '',
        dhorCycleDays: pace.dhorCycleDays != null ? String(pace.dhorCycleDays) : '',
      });
      return;
    }

    setValues((current) => ({
      ...current,
      unit: baseUnits.includes(current.unit) ? current.unit : defaultUnit,
    }));
  }, [baseUnits, defaultUnit, pace]);

  const { mutate: setPace, isPending } = useSetPace({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: getPaceQueryKey(courseId) });
      },
    },
  });

  function updateField<K extends keyof PaceFormValues>(key: K, next: PaceFormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  const unitOptions: PickerOption[] = baseUnits.map((value) => ({
    label: t(`classes.pace.unitOptions.${value}`),
    value,
  }));
  const flowUnitOptions: PickerOption[] = FLOW_UNITS.map((value) => ({
    label: t(`classes.pace.unitOptions.${value}`),
    value,
  }));

  function onSubmit() {
    const errors: FieldErrors = {};
    if (isQuran) {
      if (!values.sabakUnit) errors.sabakUnit = t('classes.pace.validation.flowUnitRequired');
      if (!values.sabakAmount.trim()) errors.sabakAmount = t('classes.pace.validation.flowAmountRequired');
      if (!values.sabqiUnit) errors.sabqiUnit = t('classes.pace.validation.flowUnitRequired');
      if (!values.sabqiAmount.trim()) errors.sabqiAmount = t('classes.pace.validation.flowAmountRequired');
      if (!values.dhorUnit) errors.dhorUnit = t('classes.pace.validation.flowUnitRequired');
      if (!values.dhorAmount.trim()) errors.dhorAmount = t('classes.pace.validation.flowAmountRequired');
      if (!values.dhorCycleDays.trim()) errors.dhorCycleDays = t('classes.pace.validation.dhorCycleDaysRequired');
    }
    setFieldErrors(errors);
    if (Object.values(errors).some(Boolean)) return;

    setPace({
      courseId,
      data: {
        unit: values.unit,
        amountPerSession: Number(values.amountPerSession) || 0.01,
        sessionsPerWeek: values.sessionsPerWeek.trim() ? Number(values.sessionsPerWeek) : undefined,
        sabak:
          isQuran && values.sabakUnit && values.sabakAmount.trim()
            ? { unit: values.sabakUnit, amount: Number(values.sabakAmount) }
            : undefined,
        sabqi:
          isQuran && values.sabqiUnit && values.sabqiAmount.trim()
            ? { unit: values.sabqiUnit, amount: Number(values.sabqiAmount) }
            : undefined,
        dhor:
          isQuran && values.dhorUnit && values.dhorAmount.trim()
            ? { unit: values.dhorUnit, amount: Number(values.dhorAmount) }
            : undefined,
        dhorCycleDays: isQuran && values.dhorCycleDays.trim() ? Number(values.dhorCycleDays) : undefined,
      },
    });
  }

  if (isLoading) {
    return <Spinner />;
  }

  return (
    <SettingsCard style={styles.card}>
      <ThemedText type="smallBold">{t('classes.pace.title')}</ThemedText>

      <Picker
        label={t('classes.pace.fields.unit')}
        options={unitOptions}
        value={values.unit}
        onValueChange={(value) => updateField('unit', value as PaceUnitEnumKey)}
        disabled={!canUpdate || isPending}
      />
      <FormTextField
        label={t('classes.pace.fields.amountPerSession')}
        value={values.amountPerSession}
        onChangeText={(text) => updateField('amountPerSession', text)}
        keyboardType="decimal-pad"
        editable={canUpdate && !isPending}
      />
      <FormTextField
        label={t('classes.pace.fields.sessionsPerWeek')}
        value={values.sessionsPerWeek}
        onChangeText={(text) => updateField('sessionsPerWeek', text)}
        keyboardType="number-pad"
        editable={canUpdate && !isPending}
      />

      {isQuran && (
        <>
          <FlowFields
            title={t('classes.pace.flows.sabak')}
            unitValue={values.sabakUnit}
            amountValue={values.sabakAmount}
            unitError={fieldErrors.sabakUnit}
            amountError={fieldErrors.sabakAmount}
            disabled={!canUpdate || isPending}
            onUnitChange={(value) => updateField('sabakUnit', value)}
            onAmountChange={(text) => updateField('sabakAmount', text)}
            unitOptions={flowUnitOptions}
            t={t}
          />
          <FlowFields
            title={t('classes.pace.flows.sabqi')}
            unitValue={values.sabqiUnit}
            amountValue={values.sabqiAmount}
            unitError={fieldErrors.sabqiUnit}
            amountError={fieldErrors.sabqiAmount}
            disabled={!canUpdate || isPending}
            onUnitChange={(value) => updateField('sabqiUnit', value)}
            onAmountChange={(text) => updateField('sabqiAmount', text)}
            unitOptions={flowUnitOptions}
            t={t}
          />
          <FlowFields
            title={t('classes.pace.flows.dhor')}
            unitValue={values.dhorUnit}
            amountValue={values.dhorAmount}
            unitError={fieldErrors.dhorUnit}
            amountError={fieldErrors.dhorAmount}
            disabled={!canUpdate || isPending}
            onUnitChange={(value) => updateField('dhorUnit', value)}
            onAmountChange={(text) => updateField('dhorAmount', text)}
            unitOptions={flowUnitOptions}
            t={t}>
            <FormTextField
              label={t('classes.pace.fields.dhorCycleDays')}
              value={values.dhorCycleDays}
              onChangeText={(text) => updateField('dhorCycleDays', text)}
              keyboardType="number-pad"
              error={fieldErrors.dhorCycleDays}
              editable={canUpdate && !isPending}
            />
          </FlowFields>
        </>
      )}

      {canUpdate && (
        <SubmitButton label={t('classes.pace.submit')} onPress={onSubmit} isPending={isPending} />
      )}
    </SettingsCard>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: Spacing.three,
  },
  flowSection: {
    gap: Spacing.two,
    paddingTop: Spacing.two,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
});
