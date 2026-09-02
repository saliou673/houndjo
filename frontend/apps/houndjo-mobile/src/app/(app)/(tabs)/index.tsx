import { Image } from 'expo-image';
import { Platform, ScrollView, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useGetCurrentUserPermissions, useGetUserDetails } from '@api-client';

import { SettingsRow } from '@/components/settings-row';
import { SettingsSection } from '@/components/settings-section';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { WebBadge } from '@/components/web-badge';
import { BottomTabInset, MaxContentWidth, Radius, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export default function HomeScreen() {
  const safeAreaInsets = useSafeAreaInsets();
  const insets = {
    ...safeAreaInsets,
    bottom: safeAreaInsets.bottom + BottomTabInset + Spacing.three,
  };
  const theme = useTheme();
  const { data: user } = useGetUserDetails();
  const { data: permissions } = useGetCurrentUserPermissions();

  const canReadUsers = (permissions ?? []).some((permission) => permission.code === 'user:read');
  const canReadRoleGroups = (permissions ?? []).some(
    (permission) => permission.code === 'role-group:read'
  );

  const contentPlatformStyle = Platform.select({
    android: {
      paddingTop: insets.top,
      paddingLeft: insets.left,
      paddingRight: insets.right,
      paddingBottom: insets.bottom,
    },
    web: {
      paddingTop: Spacing.six,
      paddingBottom: Spacing.four,
    },
  });

  return (
    <ScrollView
      style={[styles.scrollView, { backgroundColor: theme.background }]}
      contentInset={insets}
      contentContainerStyle={[styles.contentContainer, contentPlatformStyle]}>
      <ThemedView style={styles.container}>
        <ThemedView style={styles.heroSection}>
          <Image source={require('@/assets/images/icon.png')} style={styles.appIcon} />
          <ThemedText type="title" style={styles.centerText}>
            Welcome back{user?.firstName ? `, ${user.firstName}` : ''}
          </ThemedText>
          <ThemedText themeColor="textSecondary" style={styles.centerText}>
            Auth, role-based permissions, and admin tooling are already wired up. This starter kit
            is ready for you to build your product&apos;s actual domain on top of it.
          </ThemedText>
        </ThemedView>

        <ThemedView style={styles.sectionsWrapper}>
          <SettingsSection title="Quick links">
            <SettingsRow
              href="/settings/profile"
              title="Profile"
              icon={{ ios: 'person.crop.circle', android: 'account_circle', web: 'account_circle' }}
            />
            <SettingsRow
              href="/settings/account"
              title="Account & security"
              icon={{ ios: 'lock.shield', android: 'shield', web: 'shield' }}
            />
            {canReadUsers && (
              <SettingsRow
                href="/users"
                title="Users"
                icon={{ ios: 'person.2', android: 'group', web: 'group' }}
              />
            )}
            {canReadRoleGroups && (
              <SettingsRow
                href="/role-groups"
                title="Role groups"
                icon={{ ios: 'checkmark.shield', android: 'verified_user', web: 'verified_user' }}
              />
            )}
          </SettingsSection>
        </ThemedView>
        {Platform.OS === 'web' && <WebBadge />}
      </ThemedView>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scrollView: {
    flex: 1,
  },
  contentContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
  container: {
    maxWidth: MaxContentWidth,
    flexGrow: 1,
  },
  heroSection: {
    gap: Spacing.three,
    alignItems: 'center',
    paddingHorizontal: Spacing.four,
    paddingVertical: Spacing.six,
  },
  appIcon: {
    width: 64,
    height: 64,
    borderRadius: Radius.card / 2,
  },
  centerText: {
    textAlign: 'center',
  },
  sectionsWrapper: {
    gap: Spacing.five,
    paddingHorizontal: Spacing.four,
    paddingTop: Spacing.three,
  },
});
