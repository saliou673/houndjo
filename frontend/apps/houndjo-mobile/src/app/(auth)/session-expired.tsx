import { useTranslation } from 'react-i18next';
import { Stack, router } from 'expo-router';

import { ErrorScreen } from '@/components/error-screen';

export default function SessionExpiredScreen() {
  const { t } = useTranslation();

  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <ErrorScreen
        code="401"
        title={t('errors.unauthorized.title')}
        description={t('errors.unauthorized.description')}
        actions={[{ label: t('errors.backToHome'), onPress: () => router.replace('/sign-in') }]}
      />
    </>
  );
}
