import { Image } from 'expo-image';
import { Pressable, StyleSheet, View } from 'react-native';
import { useRouter } from 'expo-router';
import { SymbolView } from 'expo-symbols';
import { useGetUserDetails } from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Radius, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

const AVATAR_SIZE = 52;

function getInitials(firstName?: string, lastName?: string): string {
  const initials = `${firstName?.trim().charAt(0) ?? ''}${lastName?.trim().charAt(0) ?? ''}`;
  return initials.toUpperCase() || '?';
}

export function ProfileSummaryCard() {
  const router = useRouter();
  const theme = useTheme();
  const { data: user } = useGetUserDetails();

  if (!user) return null;

  return (
    <Pressable
      accessibilityRole="button"
      onPress={() => router.push('/settings/profile')}
      style={({ pressed }) => pressed && styles.pressed}>
      <SettingsCard style={styles.card}>
        {user.imageUrl ? (
          <Image source={{ uri: user.imageUrl }} style={styles.avatar} />
        ) : (
          <ThemedView type="backgroundSelected" style={[styles.avatar, styles.avatarFallback]}>
            <ThemedText type="smallBold">{getInitials(user.firstName, user.lastName)}</ThemedText>
          </ThemedView>
        )}

        <View style={styles.textColumn}>
          <ThemedText type="smallBold" numberOfLines={1}>
            {user.firstName} {user.lastName}
          </ThemedText>
          <ThemedText type="small" themeColor="textSecondary" numberOfLines={1}>
            {user.email}
          </ThemedText>
        </View>

        <SymbolView
          name={{ ios: 'chevron.right', android: 'chevron_right', web: 'chevron_right' }}
          size={14}
          weight="semibold"
          tintColor={theme.textSecondary}
        />
      </SettingsCard>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  pressed: {
    opacity: 0.7,
  },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.three,
  },
  avatar: {
    width: AVATAR_SIZE,
    height: AVATAR_SIZE,
    borderRadius: Radius.bubble,
  },
  avatarFallback: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  textColumn: {
    flex: 1,
    gap: Spacing.half,
  },
});
