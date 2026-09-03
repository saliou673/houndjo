import { StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useTranslation } from 'react-i18next';

import { Text } from '@/components/ui/text';
import { View } from '@/components/ui/view';
import { useColor } from '@/hooks/useColor';
import { useIsOnline } from '@/hooks/use-is-online';

export function OfflineBanner() {
  const { t } = useTranslation();
  const insets = useSafeAreaInsets();
  const isOnline = useIsOnline();
  const backgroundColor = useColor('destructive');
  const textColor = useColor('destructiveForeground');

  if (isOnline) {
    return null;
  }

  return (
    <View
      pointerEvents="none"
      style={[styles.container, { backgroundColor, paddingTop: insets.top }]}>
      <Text style={[styles.text, { color: textColor }]}>{t('common.offlineBanner')}</Text>
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
    alignItems: 'center',
    paddingBottom: 8,
  },
  text: {
    fontSize: 13,
    fontWeight: '600',
  },
});
