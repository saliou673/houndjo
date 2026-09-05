import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useGetProgressState } from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { ThemedText } from '@/components/themed-text';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Spinner } from '@/components/ui/spinner';
import { View } from '@/components/ui/view';
import { Spacing } from '@/constants/theme';

type ProgressStateCardProps = {
  studentId: number;
  courseId: number;
};

export function ProgressStateCard({ studentId, courseId }: ProgressStateCardProps) {
  const { t } = useTranslation();
  const { data, isLoading, isError } = useGetProgressState(studentId, { courseId });

  return (
    <SettingsCard style={styles.card}>
      <ThemedText type="smallBold">{t('classes.progress.state.title')}</ThemedText>

      {isLoading && <Spinner />}
      {isError && (
        <ThemedText type="small" themeColor="danger">
          {t('classes.progress.state.errorFallback')}
        </ThemedText>
      )}

      {data && (
        <>
          <View style={styles.row}>
            <View style={styles.flowColumn}>
              <ThemedText type="small" themeColor="textSecondary">
                {t('classes.progress.state.sabak')}
              </ThemedText>
              <ThemedText type="small">
                {data.sabak
                  ? t('classes.progress.state.portionLabel', {
                      fromSurah: data.sabak.fromSurah,
                      fromVerse: data.sabak.fromVerse,
                      toSurah: data.sabak.toSurah,
                      toVerse: data.sabak.toVerse,
                    })
                  : t('classes.progress.state.notRecorded')}
              </ThemedText>
            </View>
            <View style={styles.flowColumn}>
              <ThemedText type="small" themeColor="textSecondary">
                {t('classes.progress.state.sabqi')}
              </ThemedText>
              <ThemedText type="small">
                {data.sabqi
                  ? t('classes.progress.state.portionLabel', {
                      fromSurah: data.sabqi.fromSurah,
                      fromVerse: data.sabqi.fromVerse,
                      toSurah: data.sabqi.toSurah,
                      toVerse: data.sabqi.toVerse,
                    })
                  : t('classes.progress.state.notRecorded')}
              </ThemedText>
            </View>
          </View>

          <ThemedText type="small" themeColor="textSecondary">
            {t('classes.progress.state.coveredJuz', { count: data.coveredJuz?.length ?? 0 })}
          </ThemedText>

          {data.stalePortions && data.stalePortions.length > 0 ? (
            <Alert variant="destructive">
              <AlertTitle>{t('classes.progress.state.staleTitle')}</AlertTitle>
              {data.stalePortions.map((portion) => (
                <AlertDescription key={portion.juz}>
                  {t('classes.progress.state.staleJuz', { juz: portion.juz, days: portion.daysSince })}
                </AlertDescription>
              ))}
            </Alert>
          ) : (
            <ThemedText type="small" themeColor="textSecondary">
              {t('classes.progress.state.noStale')}
            </ThemedText>
          )}
        </>
      )}
    </SettingsCard>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: Spacing.two,
  },
  row: {
    flexDirection: 'row',
    gap: Spacing.three,
  },
  flowColumn: {
    flex: 1,
    gap: Spacing.half,
  },
});
