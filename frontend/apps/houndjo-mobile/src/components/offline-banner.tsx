import { StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useTranslation } from 'react-i18next';

import { useIsOnline } from '@/hooks/use-is-online';

export function OfflineBanner() {
  const { t } = useTranslation();
  const insets = useSafeAreaInsets();
  const isOnline = useIsOnline();

  if (isOnline) {
    return null;
  }

  return (
    <View pointerEvents="none" style={[styles.container, { paddingTop: insets.top }]}>
      <Text style={styles.text}>{t('common.offlineBanner')}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    zIndex: 998,
    backgroundColor: '#DC2626',
    alignItems: 'center',
    paddingBottom: 8,
  },
  text: {
    color: '#ffffff',
    fontSize: 13,
    fontWeight: '600',
  },
});
