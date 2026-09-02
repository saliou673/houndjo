import { useTranslation } from 'react-i18next';
import { Stack, router } from 'expo-router';

import { ErrorScreen } from '@/components/error-screen';

export default function ForbiddenScreen() {
  const { t } = useTranslation();

  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <ErrorScreen
        code="403"
        title={t('errors.forbidden.title')}
        description={t('errors.forbidden.description')}
        actions={[
          { label: t('errors.goBack'), onPress: () => router.back(), variant: 'outline' },
          { label: t('errors.backToHome'), onPress: () => router.replace('/') },
        ]}
      />
    </>
  );
}
