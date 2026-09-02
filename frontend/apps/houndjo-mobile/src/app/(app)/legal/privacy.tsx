import { useTranslation } from 'react-i18next';
import { Stack } from 'expo-router';

import { LegalDocumentScreen, type LegalSection } from '@/components/legal-document-screen';

const CONTACT_EMAIL = 'privacy@houndjo.com';

export default function PrivacyScreen() {
  const { t } = useTranslation();
  const sections = t('legal.privacy.sections', { returnObjects: true }) as LegalSection[];

  return (
    <>
      <Stack.Screen options={{ title: t('legal.privacy.title') }} />
      <LegalDocumentScreen
        description={t('legal.privacy.lastUpdated')}
        sections={sections}
        contactEmail={CONTACT_EMAIL}
      />
    </>
  );
}
