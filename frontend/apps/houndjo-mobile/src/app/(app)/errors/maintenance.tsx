import { useTranslation } from 'react-i18next';
import { Stack } from 'expo-router';

import { ErrorScreen } from '@/components/error-screen';

export default function MaintenanceScreen() {
  const { t } = useTranslation();

  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <ErrorScreen
        code="503"
        title={t('errors.maintenance.title')}
        description={t('errors.maintenance.description')}
        actions={[{ label: t('errors.maintenance.learnMore'), onPress: () => {}, variant: 'outline' }]}
      />
    </>
  );
}
