import { StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useTranslation } from 'react-i18next';
import { Stack } from 'expo-router';
import { SymbolView } from 'expo-symbols';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export default function HelpCenterScreen() {
  const { t } = useTranslation();
  const theme = useTheme();

  return (
    <>
      <Stack.Screen options={{ title: t('legal.helpCenter.title') }} />
      <ThemedView style={styles.container}>
        <SafeAreaView style={styles.container} edges={['bottom']}>
          <ThemedView style={styles.content}>
            <SymbolView
              name={{ ios: 'questionmark.circle', android: 'help', web: 'help' }}
              size={72}
              weight="regular"
              tintColor={theme.text}
            />
            <ThemedText type="subtitle" style={styles.heading}>
              {t('legal.helpCenter.comingSoonTitle')}
            </ThemedText>
            <ThemedText type="small" themeColor="textSecondary" style={styles.body}>
              {t('legal.helpCenter.comingSoonBody')}
            </ThemedText>
          </ThemedView>
        </SafeAreaView>
      </ThemedView>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.two,
    paddingHorizontal: Spacing.four,
  },
  heading: {
    textAlign: 'center',
  },
  body: {
    textAlign: 'center',
  },
});
