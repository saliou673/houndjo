import { useTranslation } from 'react-i18next';
import { Stack } from 'expo-router';

import { LegalDocumentScreen, type LegalSection } from '@/components/legal-document-screen';

const CONTACT_EMAIL = 'legal@houndjo.com';

export default function TermsScreen() {
  const { t } = useTranslation();
  const sections = t('legal.terms.sections', { returnObjects: true }) as LegalSection[];

  return (
    <>
      <Stack.Screen options={{ title: t('legal.terms.title') }} />
      <LegalDocumentScreen
        description={t('legal.terms.lastUpdated')}
        sections={sections}
        contactEmail={CONTACT_EMAIL}
      />
    </>
  );
}
