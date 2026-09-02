import { useTranslation } from 'react-i18next';
import { Stack, router } from 'expo-router';

import { ErrorScreen } from '@/components/error-screen';

export default function ServerErrorScreen() {
  const { t } = useTranslation();

  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <ErrorScreen
        code="500"
        title={t('errors.serverErrorScreen.title')}
        description={t('errors.serverErrorScreen.description')}
        actions={[
          { label: t('errors.goBack'), onPress: () => router.back(), variant: 'outline' },
          { label: t('errors.backToHome'), onPress: () => router.replace('/') },
        ]}
      />
    </>
  );
}
