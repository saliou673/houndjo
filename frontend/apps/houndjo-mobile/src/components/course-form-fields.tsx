import { useTranslation } from 'react-i18next';
import {
  useGetJuz,
  type CourseQuranModeEnumKey,
  type CourseTypeEnumKey,
} from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { Picker, type PickerOption } from '@/components/ui/picker';

const COURSE_TYPES: CourseTypeEnumKey[] = ['QAIDA', 'QURAN', 'BOOK'];
const QURAN_MODES: CourseQuranModeEnumKey[] = ['NAZIRA', 'HIFZ'];

export type CourseFormValues = {
  name: string;
  type: CourseTypeEnumKey;
  description: string;
  qaidaLessons: string;
  quranMode?: CourseQuranModeEnumKey;
  quranScopeFromJuz?: number;
  quranScopeToJuz?: number;
  bookTitle: string;
  bookTotalChapters: string;
  bookTotalPages: string;
};

export type CourseFormFieldErrors = Partial<
  Record<
    | 'name'
    | 'qaidaLessons'
    | 'quranMode'
    | 'quranScopeFromJuz'
    | 'quranScopeToJuz'
    | 'bookTitle'
    | 'bookTotalChapters'
    | 'bookTotalPages',
    string
  >
>;

export const INITIAL_COURSE_FORM_VALUES: CourseFormValues = {
  name: '',
  type: 'QAIDA',
  description: '',
  qaidaLessons: '',
  quranMode: undefined,
  quranScopeFromJuz: undefined,
  quranScopeToJuz: undefined,
  bookTitle: '',
  bookTotalChapters: '',
  bookTotalPages: '',
};

export function validateCourseForm(
  values: CourseFormValues,
  t: (key: string) => string
): CourseFormFieldErrors {
  const errors: CourseFormFieldErrors = {};

  if (!values.name.trim()) {
    errors.name = t('classes.courseForm.validation.nameRequired');
  }

  if (values.type === 'QAIDA') {
    const lessons = parseQaidaLessons(values.qaidaLessons);
    if (lessons.length === 0) {
      errors.qaidaLessons = t('classes.courseForm.validation.qaidaLessonsRequired');
    } else if (lessons.some((lesson) => lesson.length > 150)) {
      errors.qaidaLessons = t('classes.courseForm.validation.qaidaLessonTooLong');
    }
  }

  if (values.type === 'QURAN') {
    if (!values.quranMode) {
      errors.quranMode = t('classes.courseForm.validation.quranModeRequired');
    }
    if (values.quranScopeFromJuz == null || values.quranScopeToJuz == null) {
      const message = t('classes.courseForm.validation.quranScopeRequired');
      errors.quranScopeFromJuz = errors.quranScopeFromJuz ?? message;
      errors.quranScopeToJuz = errors.quranScopeToJuz ?? message;
    } else if (values.quranScopeFromJuz > values.quranScopeToJuz) {
      errors.quranScopeToJuz = t('classes.courseForm.validation.quranScopeInvalid');
    }
  }

  if (values.type === 'BOOK' && !values.bookTitle.trim()) {
    errors.bookTitle = t('classes.courseForm.validation.bookTitleRequired');
  }

  if (values.type === 'BOOK') {
    validateBookCount(values.bookTotalChapters, 'bookTotalChapters', errors, t);
    validateBookCount(values.bookTotalPages, 'bookTotalPages', errors, t);
  }

  return errors;
}

function parseQaidaLessons(value: string): string[] {
  return value
    .split(/\r?\n/)
    .map((lesson) => lesson.trim())
    .filter(Boolean);
}

function validateBookCount(
  value: string,
  field: 'bookTotalChapters' | 'bookTotalPages',
  errors: CourseFormFieldErrors,
  t: (key: string) => string
) {
  if (value.trim() === '') return;
  const count = Number(value);
  if (!Number.isInteger(count) || count < 1 || count > 32767) {
    errors[field] = t('classes.courseForm.validation.bookCountInvalid');
  }
}

export function buildCourseRequestData(values: CourseFormValues) {
  return {
    name: values.name.trim(),
    type: values.type,
    description: values.description.trim() || undefined,
    qaidaLessons: values.type === 'QAIDA' ? parseQaidaLessons(values.qaidaLessons) : undefined,
    quranMode: values.type === 'QURAN' ? values.quranMode : undefined,
    quranScopeFromJuz: values.type === 'QURAN' ? values.quranScopeFromJuz : undefined,
    quranScopeToJuz: values.type === 'QURAN' ? values.quranScopeToJuz : undefined,
    bookTitle: values.type === 'BOOK' ? values.bookTitle.trim() : undefined,
    bookTotalChapters:
      values.type === 'BOOK' && values.bookTotalChapters.trim() !== ''
        ? Number(values.bookTotalChapters)
        : undefined,
    bookTotalPages:
      values.type === 'BOOK' && values.bookTotalPages.trim() !== ''
        ? Number(values.bookTotalPages)
        : undefined,
  };
}

type CourseFormFieldsProps = {
  values: CourseFormValues;
  errors: CourseFormFieldErrors;
  disabled?: boolean;
  onChange: <K extends keyof CourseFormValues>(key: K, value: CourseFormValues[K]) => void;
};

export function CourseFormFields({ values, errors, disabled, onChange }: CourseFormFieldsProps) {
  const { t } = useTranslation();
  const { data: juzList } = useGetJuz();

  const typeOptions: PickerOption[] = COURSE_TYPES.map((value) => ({
    label: t(`classes.courseForm.typeOptions.${value}`),
    value,
  }));

  const quranModeOptions: PickerOption[] = QURAN_MODES.map((value) => ({
    label: t(`classes.courseForm.quranModeOptions.${value}`),
    value,
  }));

  const juzOptions: PickerOption[] = (juzList ?? []).map((juz) => ({
    label: t('classes.courseForm.fields.juzOption', { number: juz.number ?? 0 }),
    value: String(juz.number ?? 0),
  }));

  return (
    <>
      <FormTextField
        label={t('classes.courseForm.fields.name')}
        value={values.name}
        onChangeText={(text) => onChange('name', text)}
        error={errors.name}
        editable={!disabled}
      />

      <Picker
        label={t('classes.courseForm.fields.type')}
        options={typeOptions}
        value={values.type}
        onValueChange={(value) => onChange('type', value as CourseTypeEnumKey)}
        disabled={disabled}
      />

      <FormTextField
        label={t('classes.courseForm.fields.description')}
        value={values.description}
        onChangeText={(text) => onChange('description', text)}
        multiline
        editable={!disabled}
      />

      {values.type === 'QAIDA' && (
        <FormTextField
          label={t('classes.courseForm.fields.qaidaLessons')}
          value={values.qaidaLessons}
          onChangeText={(text) => onChange('qaidaLessons', text)}
          error={errors.qaidaLessons}
          placeholder={t('classes.courseForm.fields.qaidaLessonsPlaceholder')}
          multiline
          editable={!disabled}
        />
      )}

      {values.type === 'QURAN' && (
        <>
          <Picker
            label={t('classes.courseForm.fields.quranMode')}
            placeholder={t('classes.courseForm.fields.quranModePlaceholder')}
            options={quranModeOptions}
            value={values.quranMode}
            onValueChange={(value) => onChange('quranMode', value as CourseQuranModeEnumKey)}
            disabled={disabled}
            error={errors.quranMode}
          />
          <Picker
            label={t('classes.courseForm.fields.quranScopeFromJuz')}
            placeholder={t('classes.courseForm.fields.quranScopePlaceholder')}
            options={juzOptions}
            searchable
            value={values.quranScopeFromJuz != null ? String(values.quranScopeFromJuz) : undefined}
            onValueChange={(value) => onChange('quranScopeFromJuz', Number(value))}
            disabled={disabled}
            error={errors.quranScopeFromJuz}
          />
          <Picker
            label={t('classes.courseForm.fields.quranScopeToJuz')}
            placeholder={t('classes.courseForm.fields.quranScopePlaceholder')}
            options={juzOptions}
            searchable
            value={values.quranScopeToJuz != null ? String(values.quranScopeToJuz) : undefined}
            onValueChange={(value) => onChange('quranScopeToJuz', Number(value))}
            disabled={disabled}
            error={errors.quranScopeToJuz}
          />
        </>
      )}

      {values.type === 'BOOK' && (
        <>
          <FormTextField
            label={t('classes.courseForm.fields.bookTitle')}
            value={values.bookTitle}
            onChangeText={(text) => onChange('bookTitle', text)}
            error={errors.bookTitle}
            editable={!disabled}
          />
          <FormTextField
            label={t('classes.courseForm.fields.bookTotalChapters')}
            value={values.bookTotalChapters}
            onChangeText={(text) => onChange('bookTotalChapters', text)}
            keyboardType="numeric"
            error={errors.bookTotalChapters}
            editable={!disabled}
          />
          <FormTextField
            label={t('classes.courseForm.fields.bookTotalPages')}
            value={values.bookTotalPages}
            onChangeText={(text) => onChange('bookTotalPages', text)}
            keyboardType="numeric"
            error={errors.bookTotalPages}
            editable={!disabled}
          />
        </>
      )}
    </>
  );
}
