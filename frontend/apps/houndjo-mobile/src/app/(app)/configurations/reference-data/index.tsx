import { useEffect, useMemo, useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useTranslation } from 'react-i18next';
import { Stack, useRouter, type Href } from 'expo-router';
import {
  useGetAppConfigurationsAsAdmin,
  useGetCategoriesAsAdmin,
  type AppConfiguration,
  type AppConfigurationCategoryEnumKey,
  type AppConfigurationFilter,
} from '@api-client';

import { SettingsCard } from '@/components/settings-card';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Button } from '@/components/ui/button';
import { RadioGroup } from '@/components/ui/radio';
import { SearchBar } from '@/components/ui/searchbar';
import { Spinner } from '@/components/ui/spinner';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

const ALL_CATEGORIES = '__all__';

const PAGE_SIZE = 20;

function ConfigurationListItem({
  configuration,
  onPress,
}: {
  configuration: AppConfiguration;
  onPress: () => void;
}) {
  const { t } = useTranslation();
  const theme = useTheme();

  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => pressed && styles.pressed}>
      <SettingsCard style={styles.card}>
        <View style={styles.cardHeader}>
          <ThemedText type="code" numberOfLines={1} style={styles.code}>
            {configuration.code}
          </ThemedText>
          <View style={[styles.badge, { backgroundColor: theme.backgroundElement }]}>
            <ThemedText
              type="small"
              themeColor={configuration.active ? 'text' : 'textSecondary'}>
              {configuration.active
                ? t('configurations.referenceData.active')
                : t('configurations.referenceData.inactive')}
            </ThemedText>
          </View>
        </View>

        <ThemedText type="small" numberOfLines={1}>
          {configuration.label}
        </ThemedText>

        {configuration.description && (
          <ThemedText type="small" themeColor="textSecondary" numberOfLines={2}>
            {configuration.description}
          </ThemedText>
        )}
      </SettingsCard>
    </Pressable>
  );
}

export default function ReferenceDataScreen() {
  const { t } = useTranslation();
  const router = useRouter();

  const [codeInput, setCodeInput] = useState('');
  const [debouncedCode, setDebouncedCode] = useState('');
  const [category, setCategory] = useState<AppConfigurationCategoryEnumKey | undefined>(undefined);
  const [page, setPage] = useState(0);

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedCode(codeInput.trim()), 300);
    return () => clearTimeout(timeout);
  }, [codeInput]);

  useEffect(() => {
    setPage(0);
  }, [debouncedCode, category]);

  const { data: categoriesData } = useGetCategoriesAsAdmin();
  const categoryOptions = categoriesData ?? [];

  const filter = useMemo<AppConfigurationFilter>(() => {
    const nextFilter: AppConfigurationFilter = {};
    if (debouncedCode) {
      nextFilter.code = { contains: debouncedCode };
    }
    if (category) {
      nextFilter.category = { equals: category };
    }
    return nextFilter;
  }, [category, debouncedCode]);

  const { data, isLoading, isFetching, isError, refetch } = useGetAppConfigurationsAsAdmin(
    { filter, pageable: { page, size: PAGE_SIZE } },
    undefined,
    { query: { placeholderData: (previous) => previous } }
  );

  const items = data?.items ?? [];
  const totalPages = data?.totalPages ?? 0;
  const canGoPrevious = page > 0;
  const canGoNext = page + 1 < totalPages;

  function handleRefresh() {
    if (page === 0) {
      void refetch();
      return;
    }
    setPage(0);
  }

  return (
    <>
      <Stack.Screen options={{ title: t('configurations.referenceData.title') }} />
      <ThemedView style={styles.container}>
        <SafeAreaView style={styles.container} edges={['bottom']}>
          <View style={styles.content}>
            <View style={styles.headerRow}>
              <ThemedText type="small" themeColor="textSecondary" style={styles.headerDescription}>
                {t('configurations.referenceData.description')}
              </ThemedText>
              <Button
                size="sm"
                onPress={() => router.push('/configurations/reference-data/create' as Href)}>
                {t('configurations.referenceData.addConfiguration')}
              </Button>
            </View>

            <SearchBar
              value={codeInput}
              onChangeText={setCodeInput}
              placeholder={t('configurations.referenceData.searchPlaceholder')}
              autoCapitalize="none"
              autoCorrect={false}
            />

            <RadioGroup
              orientation="horizontal"
              style={styles.categoryRow}
              value={category ?? ALL_CATEGORIES}
              onValueChange={(next) =>
                setCategory(
                  next === ALL_CATEGORIES ? undefined : (next as AppConfigurationCategoryEnumKey)
                )
              }
              options={[
                { value: ALL_CATEGORIES, label: t('configurations.referenceData.allCategories') },
                ...categoryOptions
                  .filter((option): option is typeof option & { value: string } => !!option.value)
                  .map((option) => ({
                    value: option.value,
                    label: option.description ?? option.value,
                  })),
              ]}
            />

            {isError && (
              <ThemedText themeColor="danger" type="small">
                {t('configurations.referenceData.errorFallback')}
              </ThemedText>
            )}

            {isLoading ? (
              <Spinner style={styles.loadingIndicator} />
            ) : (
              <FlatList
                data={items}
                keyExtractor={(configuration) => String(configuration.id ?? 0)}
                renderItem={({ item }) => (
                  <ConfigurationListItem
                    configuration={item}
                    onPress={() =>
                      router.push(`/configurations/reference-data/${item.id ?? 0}` as Href)
                    }
                  />
                )}
                style={styles.list}
                contentContainerStyle={styles.listContent}
                ItemSeparatorComponent={() => <View style={styles.separator} />}
                refreshControl={<RefreshControl refreshing={isFetching} onRefresh={handleRefresh} />}
                ListEmptyComponent={
                  <ThemedText themeColor="textSecondary" style={styles.emptyText}>
                    {t('configurations.referenceData.noResults')}
                  </ThemedText>
                }
                ListFooterComponent={
                  items.length > 0 ? (
                    <View style={styles.pager}>
                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={!canGoPrevious}
                        onPress={() => setPage((current) => Math.max(0, current - 1))}>
                        {t('configurations.referenceData.previous')}
                      </Button>

                      <ThemedText type="small" themeColor="textSecondary">
                        {t('configurations.referenceData.pageIndicator', {
                          page: page + 1,
                          totalPages: Math.max(totalPages, 1),
                        })}
                      </ThemedText>

                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={!canGoNext}
                        onPress={() => setPage((current) => current + 1)}>
                        {t('configurations.referenceData.next')}
                      </Button>
                    </View>
                  ) : null
                }
              />
            )}
          </View>
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
    width: '100%',
    maxWidth: MaxContentWidth,
    alignSelf: 'center',
    paddingHorizontal: Spacing.four,
    paddingTop: Spacing.four,
    gap: Spacing.three,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.two,
  },
  headerDescription: {
    flex: 1,
  },
  categoryRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.one,
  },
  loadingIndicator: {
    marginTop: Spacing.five,
  },
  list: {
    flex: 1,
  },
  listContent: {
    paddingBottom: BottomTabInset + Spacing.four,
  },
  separator: {
    height: Spacing.two,
  },
  emptyText: {
    textAlign: 'center',
    marginTop: Spacing.five,
  },
  card: {
    gap: Spacing.two,
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: Spacing.two,
  },
  code: {
    flex: 1,
  },
  badge: {
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
  },
  pressed: {
    opacity: 0.7,
  },
  pager: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: Spacing.three,
  },
});
