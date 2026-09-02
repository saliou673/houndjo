import { useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, FlatList, Pressable, RefreshControl, StyleSheet, TextInput, View } from 'react-native';
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
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

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
  const theme = useTheme();

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
              <Pressable
                accessibilityRole="button"
                onPress={() => router.push('/configurations/reference-data/create' as Href)}
                style={({ pressed }) => [
                  styles.addButton,
                  { backgroundColor: theme.text },
                  pressed && styles.pressed,
                ]}>
                <ThemedText type="smallBold" style={{ color: theme.background }}>
                  {t('configurations.referenceData.addConfiguration')}
                </ThemedText>
              </Pressable>
            </View>

            <TextInput
              value={codeInput}
              onChangeText={setCodeInput}
              placeholder={t('configurations.referenceData.searchPlaceholder')}
              placeholderTextColor={theme.textSecondary}
              autoCapitalize="none"
              autoCorrect={false}
              style={[
                styles.searchInput,
                {
                  backgroundColor: theme.backgroundElement,
                  borderColor: theme.backgroundSelected,
                  color: theme.text,
                },
              ]}
            />

            <View style={styles.categoryRow}>
              <Pressable
                accessibilityRole="button"
                onPress={() => setCategory(undefined)}
                style={[
                  styles.categoryChip,
                  {
                    borderColor: theme.backgroundSelected,
                    backgroundColor: !category ? theme.text : 'transparent',
                  },
                ]}>
                <ThemedText type="small" style={{ color: !category ? theme.background : theme.text }}>
                  {t('configurations.referenceData.allCategories')}
                </ThemedText>
              </Pressable>
              {categoryOptions.map((option) => {
                const selected = category === option.value;
                return (
                  <Pressable
                    key={option.value}
                    accessibilityRole="button"
                    onPress={() => setCategory(option.value)}
                    style={[
                      styles.categoryChip,
                      {
                        borderColor: theme.backgroundSelected,
                        backgroundColor: selected ? theme.text : 'transparent',
                      },
                    ]}>
                    <ThemedText
                      type="small"
                      style={{ color: selected ? theme.background : theme.text }}>
                      {option.description ?? option.value}
                    </ThemedText>
                  </Pressable>
                );
              })}
            </View>

            {isError && (
              <ThemedText themeColor="danger" type="small">
                {t('configurations.referenceData.errorFallback')}
              </ThemedText>
            )}

            {isLoading ? (
              <ActivityIndicator style={styles.loadingIndicator} />
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
                      <Pressable
                        accessibilityRole="button"
                        disabled={!canGoPrevious}
                        onPress={() => setPage((current) => Math.max(0, current - 1))}
                        style={({ pressed }) => [
                          styles.pagerButton,
                          (!canGoPrevious || pressed) && styles.pagerButtonDisabled,
                        ]}>
                        <ThemedText type="small">{t('configurations.referenceData.previous')}</ThemedText>
                      </Pressable>

                      <ThemedText type="small" themeColor="textSecondary">
                        {t('configurations.referenceData.pageIndicator', {
                          page: page + 1,
                          totalPages: Math.max(totalPages, 1),
                        })}
                      </ThemedText>

                      <Pressable
                        accessibilityRole="button"
                        disabled={!canGoNext}
                        onPress={() => setPage((current) => current + 1)}
                        style={({ pressed }) => [
                          styles.pagerButton,
                          (!canGoNext || pressed) && styles.pagerButtonDisabled,
                        ]}>
                        <ThemedText type="small">{t('configurations.referenceData.next')}</ThemedText>
                      </Pressable>
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
  addButton: {
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
  },
  searchInput: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    fontSize: 16,
  },
  categoryRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.one,
  },
  categoryChip: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.one,
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
  pagerButton: {
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
  },
  pagerButtonDisabled: {
    opacity: 0.3,
  },
});
