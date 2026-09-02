import { useTranslation } from 'react-i18next';
import { Stack } from 'expo-router';

import { LegalDocumentScreen, type LegalSection } from '@/components/legal-document-screen';

const CONTACT_EMAIL = 'privacy@houndjo.com';

export default function CookiePolicyScreen() {
  const { t } = useTranslation();
  const sections = t('legal.cookiePolicy.sections', { returnObjects: true }) as LegalSection[];

  return (
    <>
      <Stack.Screen options={{ title: t('legal.cookiePolicy.title') }} />
      <LegalDocumentScreen
        description={t('legal.cookiePolicy.lastUpdated')}
        sections={sections}
        contactEmail={CONTACT_EMAIL}
      />
    </>
  );
}
