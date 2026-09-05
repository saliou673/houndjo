import { useState } from 'react';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import {
  getProgressStateQueryKey,
  useRecordProgress,
  type CourseTypeEnumKey,
  type RecordProgressRequestFlowEnumKey,
  type RecordProgressRequestFluencyEnumKey,
} from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { Picker, type PickerOption } from '@/components/ui/picker';
import { RadioGroup } from '@/components/ui/radio';
import { View } from '@/components/ui/view';
import { Spacing } from '@/constants/theme';
import { extractApiErrorMessage } from '@/lib/api-error';

const QURAN_FLOWS: RecordProgressRequestFlowEnumKey[] = ['SABAK', 'SABQI', 'DHOR'];
const RATING_OPTIONS: RecordProgressRequestFluencyEnumKey[] = ['WEAK', 'FAIR', 'GOOD', 'EXCELLENT'];

type FieldsState = {
  fromSurah: string;
  fromVerse: string;
  toSurah: string;
  toVerse: string;
  lessonId?: string;
  chapterNo: string;
  pageNo: string;
  errorCount: string;
  fluency?: RecordProgressRequestFluencyEnumKey;
  tajweed?: RecordProgressRequestFluencyEnumKey;
  note: string;
};

const INITIAL_FIELDS: FieldsState = {
  fromSurah: '',
  fromVerse: '',
  toSurah: '',
  toVerse: '',
  lessonId: undefined,
  chapterNo: '',
  pageNo: '',
  errorCount: '0',
  fluency: undefined,
  tajweed: undefined,
  note: '',
};

type FieldErrors = Partial<Record<keyof FieldsState, string>>;

function initialFlowFor(courseType: CourseTypeEnumKey): RecordProgressRequestFlowEnumKey {
  if (courseType === 'QURAN') return 'SABAK';
  return courseType === 'QAIDA' ? 'LESSON' : 'CHAPTER';
}

type ProgressEntryCardProps = {
  courseId: number;
  sessionId: number;
  studentId: number;
  courseType: CourseTypeEnumKey;
  qaidaLessons: string[];
};

export function ProgressEntryCard({
  courseId,
  sessionId,
  studentId,
  courseType,
  qaidaLessons,
}: ProgressEntryCardProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const isQuran = courseType === 'QURAN';
  const isQuranFlow = (candidate: RecordProgressRequestFlowEnumKey) =>
    candidate === 'SABAK' || candidate === 'SABQI' || candidate === 'DHOR';

  const [flow, setFlow] = useState<RecordProgressRequestFlowEnumKey>(() => initialFlowFor(courseType));
  const [fields, setFields] = useState<FieldsState>(INITIAL_FIELDS);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  const { mutateAsync, isPending } = useRecordProgress({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof FieldsState>(key: K, next: FieldsState[K]) {
    setFields((current) => ({ ...current, [key]: next }));
  }

  function switchFlow(next: RecordProgressRequestFlowEnumKey) {
    setFlow(next);
    setFields(INITIAL_FIELDS);
    setFieldErrors({});
    setFormError(null);
  }

  const lessonOptions: PickerOption[] = qaidaLessons.map((lesson, index) => ({
    label: lesson,
    value: String(index),
  }));
  const ratingOptions: PickerOption[] = RATING_OPTIONS.map((value) => ({
    label: t(`classes.progress.entry.ratingOptions.${value}`),
    value,
  }));

  function isValidNumber(value: string): boolean {
    const trimmed = value.trim();
    return trimmed !== '' && Number.isFinite(Number(trimmed));
  }

  function validate(): FieldErrors {
    const errors: FieldErrors = {};
    if (isQuranFlow(flow)) {
      if (!fields.fromSurah.trim() || !fields.fromVerse.trim()) {
        errors.fromSurah = t('classes.progress.entry.validation.fromRequired');
      } else if (!isValidNumber(fields.fromSurah) || !isValidNumber(fields.fromVerse)) {
        errors.fromSurah = t('classes.progress.entry.validation.fromInvalid');
      }
      if (!fields.toSurah.trim() || !fields.toVerse.trim()) {
        errors.toSurah = t('classes.progress.entry.validation.toRequired');
      } else if (!isValidNumber(fields.toSurah) || !isValidNumber(fields.toVerse)) {
        errors.toSurah = t('classes.progress.entry.validation.toInvalid');
      }
    } else if (flow === 'LESSON') {
      if (!fields.lessonId) {
        errors.lessonId = t('classes.progress.entry.validation.lessonRequired');
      }
    } else if (flow === 'CHAPTER') {
      if (!fields.chapterNo.trim()) {
        errors.chapterNo = t('classes.progress.entry.validation.chapterRequired');
      } else if (!isValidNumber(fields.chapterNo)) {
        errors.chapterNo = t('classes.progress.entry.validation.chapterInvalid');
      }
      if (!fields.pageNo.trim()) {
        errors.pageNo = t('classes.progress.entry.validation.pageRequired');
      } else if (!isValidNumber(fields.pageNo)) {
        errors.pageNo = t('classes.progress.entry.validation.pageInvalid');
      }
    }
    if (!fields.fluency) {
      errors.fluency = t('classes.progress.entry.validation.fluencyRequired');
    }
    return errors;
  }

  async function onSubmit() {
    const errors = validate();
    setFieldErrors(errors);
    setFormError(null);
    if (Object.values(errors).some(Boolean) || !fields.fluency) return;

    try {
      await mutateAsync({
        data: {
          studentId,
          courseId,
          sessionId,
          flow,
          fromSurah: isQuranFlow(flow) ? Number(fields.fromSurah) : undefined,
          fromVerse: isQuranFlow(flow) ? Number(fields.fromVerse) : undefined,
          toSurah: isQuranFlow(flow) ? Number(fields.toSurah) : undefined,
          toVerse: isQuranFlow(flow) ? Number(fields.toVerse) : undefined,
          lessonId: flow === 'LESSON' && fields.lessonId ? Number(fields.lessonId) : undefined,
          chapterNo: flow === 'CHAPTER' ? Number(fields.chapterNo) : undefined,
          pageNo: flow === 'CHAPTER' ? Number(fields.pageNo) : undefined,
          errorCount: Number(fields.errorCount) || 0,
          fluency: fields.fluency,
          tajweed: fields.tajweed,
          // Single-gesture validation: this screen always records a VALIDATED
          // entry directly. Queuing this mutation for later (offline mode,
          // V3) would hook in right here, ahead of mutateAsync.
          status: 'VALIDATED',
          note: fields.note.trim() || undefined,
        },
      });
      await queryClient.invalidateQueries({ queryKey: getProgressStateQueryKey(studentId, { courseId }) });
      showToast(t('classes.progress.entry.successToast'), 'success');
      setFields(INITIAL_FIELDS);
      setFieldErrors({});
    } catch (error) {
      if (error instanceof AxiosError) {
        setFormError(extractApiErrorMessage(error, t('errors.generic')));
        return;
      }
      setFormError(t('errors.generic'));
    }
  }

  return (
    <SettingsCard style={styles.card}>
      <ThemedText type="smallBold">{t('classes.progress.entry.title')}</ThemedText>

      {isQuran && (
        <RadioGroup
          orientation="horizontal"
          style={styles.flowRow}
          value={flow}
          onValueChange={(next) => switchFlow(next as RecordProgressRequestFlowEnumKey)}
          disabled={isPending}
          options={QURAN_FLOWS.map((value) => ({
            value,
            label: t(`classes.progress.entry.tabs.${value}`),
          }))}
        />
      )}

      {isQuranFlow(flow) && (
        <>
          <View style={styles.fieldRow}>
            <View style={styles.fieldHalf}>
              <FormTextField
                label={t('classes.progress.entry.fields.fromSurah')}
                value={fields.fromSurah}
                onChangeText={(text) => updateField('fromSurah', text)}
                keyboardType="number-pad"
                error={fieldErrors.fromSurah}
                editable={!isPending}
              />
            </View>
            <View style={styles.fieldHalf}>
              <FormTextField
                label={t('classes.progress.entry.fields.fromVerse')}
                value={fields.fromVerse}
                onChangeText={(text) => updateField('fromVerse', text)}
                keyboardType="number-pad"
                editable={!isPending}
              />
            </View>
          </View>
          <View style={styles.fieldRow}>
            <View style={styles.fieldHalf}>
              <FormTextField
                label={t('classes.progress.entry.fields.toSurah')}
                value={fields.toSurah}
                onChangeText={(text) => updateField('toSurah', text)}
                keyboardType="number-pad"
                error={fieldErrors.toSurah}
                editable={!isPending}
              />
            </View>
            <View style={styles.fieldHalf}>
              <FormTextField
                label={t('classes.progress.entry.fields.toVerse')}
                value={fields.toVerse}
                onChangeText={(text) => updateField('toVerse', text)}
                keyboardType="number-pad"
                editable={!isPending}
              />
            </View>
          </View>
        </>
      )}

      {flow === 'LESSON' && (
        <Picker
          label={t('classes.progress.entry.fields.lesson')}
          placeholder={t('classes.progress.entry.fields.lessonPlaceholder')}
          options={lessonOptions}
          value={fields.lessonId}
          onValueChange={(value) => updateField('lessonId', value)}
          error={fieldErrors.lessonId}
          disabled={isPending}
        />
      )}

      {flow === 'CHAPTER' && (
        <View style={styles.fieldRow}>
          <View style={styles.fieldHalf}>
            <FormTextField
              label={t('classes.progress.entry.fields.chapterNo')}
              value={fields.chapterNo}
              onChangeText={(text) => updateField('chapterNo', text)}
              keyboardType="number-pad"
              error={fieldErrors.chapterNo}
              editable={!isPending}
            />
          </View>
          <View style={styles.fieldHalf}>
            <FormTextField
              label={t('classes.progress.entry.fields.pageNo')}
              value={fields.pageNo}
              onChangeText={(text) => updateField('pageNo', text)}
              keyboardType="number-pad"
              error={fieldErrors.pageNo}
              editable={!isPending}
            />
          </View>
        </View>
      )}

      <FormTextField
        label={t('classes.progress.entry.fields.errorCount')}
        value={fields.errorCount}
        onChangeText={(text) => updateField('errorCount', text)}
        keyboardType="number-pad"
        editable={!isPending}
      />

      <Picker
        label={t('classes.progress.entry.fields.fluency')}
        placeholder={t('classes.progress.entry.fields.fluencyPlaceholder')}
        options={ratingOptions}
        value={fields.fluency}
        onValueChange={(value) => updateField('fluency', value as RecordProgressRequestFluencyEnumKey)}
        error={fieldErrors.fluency}
        disabled={isPending}
      />

      <Picker
        label={t('classes.progress.entry.fields.tajweed')}
        placeholder={t('classes.progress.entry.fields.fluencyPlaceholder')}
        options={ratingOptions}
        value={fields.tajweed}
        onValueChange={(value) => updateField('tajweed', value as RecordProgressRequestFluencyEnumKey)}
        disabled={isPending}
      />

      <FormTextField
        label={t('classes.progress.entry.fields.note')}
        value={fields.note}
        onChangeText={(text) => updateField('note', text)}
        multiline
        editable={!isPending}
      />

      {formError && (
        <ThemedText type="small" themeColor="danger">
          {formError}
        </ThemedText>
      )}

      <SubmitButton label={t('classes.progress.entry.submit')} onPress={() => void onSubmit()} isPending={isPending} />
    </SettingsCard>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: Spacing.three,
  },
  flowRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.one,
  },
  fieldRow: {
    flexDirection: 'row',
    gap: Spacing.two,
  },
  fieldHalf: {
    flex: 1,
  },
});
