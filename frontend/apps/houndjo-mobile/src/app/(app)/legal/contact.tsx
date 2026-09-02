import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import { Stack } from 'expo-router';
import { useGetUserDetails, useSendContactForm } from '@api-client';

import { FormTextField } from '@/components/form-text-field';
import { SettingsCard } from '@/components/settings-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SubmitButton } from '@/components/submit-button';
import { ThemedText } from '@/components/themed-text';
import { showToast } from '@/components/toast/toast-store';
import { extractApiErrorMessage } from '@/lib/api-error';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

type FormValues = {
  name: string;
  email: string;
  subject: string;
  message: string;
};

type FieldErrors = Partial<Record<keyof FormValues, string>>;

const INITIAL_VALUES: FormValues = {
  name: '',
  email: '',
  subject: '',
  message: '',
};

export default function ContactScreen() {
  const { t } = useTranslation();
  const { data: user } = useGetUserDetails();

  const [values, setValues] = useState<FormValues>(INITIAL_VALUES);
  const [prefilled, setPrefilled] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (user && !prefilled) {
      setValues((current) => ({
        ...current,
        name: `${user.firstName} ${user.lastName}`.trim(),
        email: user.email,
      }));
      setPrefilled(true);
    }
  }, [user, prefilled]);

  const { mutateAsync, isPending } = useSendContactForm({
    mutation: { meta: { skipGlobalErrorToast: true } },
  });

  function updateField<K extends keyof FormValues>(key: K, next: FormValues[K]) {
    setValues((current) => ({ ...current, [key]: next }));
  }

  function validate(current: FormValues): FieldErrors {
    const errors: FieldErrors = {};

    if (!current.name.trim()) {
      errors.name = t('legal.contact.validation.nameRequired');
    }
    if (!current.email.trim()) {
      errors.email = t('legal.contact.validation.emailRequired');
    } else if (!EMAIL_PATTERN.test(current.email.trim())) {
      errors.email = t('legal.contact.validation.emailInvalid');
    }
    if (!current.subject.trim()) {
      errors.subject = t('legal.contact.validation.subjectRequired');
    }
    if (!current.message.trim()) {
      errors.message = t('legal.contact.validation.messageRequired');
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
          email: values.email.trim(),
          subject: values.subject.trim(),
          message: values.message.trim(),
        },
      });
      showToast(t('legal.contact.toastSent'), 'success');
      setValues({ ...INITIAL_VALUES, name: values.name, email: values.email });
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
      <Stack.Screen options={{ title: t('legal.contact.title') }} />
      <SettingsListScreen description={t('legal.contact.description')}>
        <SettingsCard>
          <FormTextField
            label={t('legal.contact.fields.name')}
            value={values.name}
            onChangeText={(text) => updateField('name', text)}
            error={fieldErrors.name}
            editable={!isPending}
          />

          <FormTextField
            label={t('legal.contact.fields.email')}
            value={values.email}
            onChangeText={(text) => updateField('email', text)}
            error={fieldErrors.email}
            autoCapitalize="none"
            autoCorrect={false}
            keyboardType="email-address"
            editable={!isPending}
          />

          <FormTextField
            label={t('legal.contact.fields.subject')}
            value={values.subject}
            onChangeText={(text) => updateField('subject', text)}
            error={fieldErrors.subject}
            editable={!isPending}
          />

          <FormTextField
            label={t('legal.contact.fields.message')}
            value={values.message}
            onChangeText={(text) => updateField('message', text)}
            error={fieldErrors.message}
            multiline
            numberOfLines={6}
            editable={!isPending}
          />

          {formError && (
            <ThemedText type="small" themeColor="danger">
              {formError}
            </ThemedText>
          )}

          <SubmitButton
            label={t('legal.contact.submit')}
            onPress={() => void onSubmit()}
            isPending={isPending}
          />
        </SettingsCard>
      </SettingsListScreen>
    </>
  );
}
