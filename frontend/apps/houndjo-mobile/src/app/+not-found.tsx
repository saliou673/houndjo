import { useTranslation } from 'react-i18next';
import { Stack, router } from 'expo-router';

import { ErrorScreen } from '@/components/error-screen';

export default function NotFoundScreen() {
  const { t } = useTranslation();

  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <ErrorScreen
        code="404"
        title={t('errors.notFound.title')}
        description={t('errors.notFound.description')}
        actions={[
          { label: t('errors.goBack'), onPress: () => router.back(), variant: 'outline' },
          { label: t('errors.backToHome'), onPress: () => router.replace('/') },
        ]}
      />
    </>
  );
}
