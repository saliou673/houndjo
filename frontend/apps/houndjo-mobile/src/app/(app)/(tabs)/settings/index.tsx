import { Stack } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { useGetCurrentUserPermissions } from '@api-client';

import { LogoutButton } from '@/components/logout-button';
import { ProfileSummaryCard } from '@/components/profile-summary-card';
import { SettingsListScreen } from '@/components/settings-list-screen';
import { SettingsRow } from '@/components/settings-row';
import { SettingsSection } from '@/components/settings-section';

export default function SettingsHomeScreen() {
  const { t } = useTranslation();
  const { data: permissions } = useGetCurrentUserPermissions();

  const canReadUsers = (permissions ?? []).some((permission) => permission.code === 'user:read');
  const canReadRoleGroups = (permissions ?? []).some(
    (permission) => permission.code === 'role-group:read'
  );
  const canManageConfigurations = (permissions ?? []).some(
    (permission) => permission.code === 'config:manage'
  );

  return (
    <>
      <Stack.Screen options={{ title: t('settings.nav.title') }} />
      <SettingsListScreen description={t('settings.nav.description')}>
        <ProfileSummaryCard />

        <SettingsSection title={t('settings.nav.sectionAccount')}>
          <SettingsRow
            href="/settings/profile"
            title={t('settings.nav.profile')}
            icon={{ ios: 'person.crop.circle', android: 'account_circle', web: 'account_circle' }}
          />
          <SettingsRow
            href="/settings/account"
            title={t('settings.nav.account')}
            icon={{ ios: 'lock.shield', android: 'shield', web: 'shield' }}
          />
        </SettingsSection>

        <SettingsSection title={t('settings.nav.sectionPreferences')}>
          <SettingsRow
            href="/settings/appearance"
            title={t('settings.nav.appearance')}
            icon={{ ios: 'paintpalette', android: 'palette', web: 'palette' }}
          />
          <SettingsRow
            href="/settings/notifications"
            title={t('settings.nav.notifications')}
            icon={{ ios: 'bell', android: 'notifications', web: 'notifications' }}
          />
          <SettingsRow
            href="/settings/display"
            title={t('settings.nav.display')}
            icon={{ ios: 'textformat.size', android: 'format_size', web: 'format_size' }}
          />
          <SettingsRow
            href="/settings/language"
            title={t('settings.nav.language')}
            icon={{ ios: 'globe', android: 'language', web: 'language' }}
          />
        </SettingsSection>

        <SettingsSection title={t('legal.nav.sectionTitle')}>
          <SettingsRow
            href="/legal/terms"
            title={t('legal.nav.terms')}
            icon={{ ios: 'doc.text', android: 'description', web: 'description' }}
          />
          <SettingsRow
            href="/legal/privacy"
            title={t('legal.nav.privacy')}
            icon={{ ios: 'hand.raised', android: 'privacy_tip', web: 'privacy_tip' }}
          />
          <SettingsRow
            href="/legal/cookie-policy"
            title={t('legal.nav.cookiePolicy')}
            icon={{ ios: 'circle.grid.2x2', android: 'cookie', web: 'cookie' }}
          />
          <SettingsRow
            href="/legal/contact"
            title={t('legal.nav.contact')}
            icon={{ ios: 'envelope', android: 'mail', web: 'mail' }}
          />
          <SettingsRow
            href="/legal/help-center"
            title={t('legal.nav.helpCenter')}
            icon={{ ios: 'questionmark.circle', android: 'help', web: 'help' }}
          />
        </SettingsSection>

        {(canReadUsers || canReadRoleGroups || canManageConfigurations) && (
          <SettingsSection title={t('settings.nav.sectionAdmin')}>
            {canReadUsers && (
              <SettingsRow
                href="/users"
                title={t('settings.nav.users')}
                icon={{ ios: 'person.2', android: 'group', web: 'group' }}
              />
            )}
            {canReadRoleGroups && (
              <SettingsRow
                href="/role-groups"
                title={t('settings.nav.roleGroups')}
                icon={{ ios: 'checkmark.shield', android: 'verified_user', web: 'verified_user' }}
              />
            )}
            {canManageConfigurations && (
              <SettingsRow
                href="/configurations"
                title={t('settings.nav.configurations')}
                icon={{ ios: 'slider.horizontal.3', android: 'tune', web: 'tune' }}
              />
            )}
          </SettingsSection>
        )}

        <SettingsSection>
          <LogoutButton />
        </SettingsSection>
      </SettingsListScreen>
    </>
  );
}
