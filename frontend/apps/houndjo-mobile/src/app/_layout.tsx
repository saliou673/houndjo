import { configureApiClient } from '@api-client';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Stack } from 'expo-router';
import { useEffect, useState } from 'react';
import { I18nextProvider } from 'react-i18next';

import { AnimatedSplashOverlay } from '@/components/animated-icon';
import { AppErrorBoundary } from '@/components/error-boundary';
import { OfflineBanner } from '@/components/offline-banner';
import { Toaster } from '@/components/toast/toaster';
import { apiBaseUrl } from '@/constants/env';
import { AppTextSizeProvider } from '@/context/text-size-provider';
import { AuthProvider, useAuth } from '@/hooks/use-auth';
import i18n, { hydrateStoredLanguage } from '@/i18n';
import { hydrateAccessToken, setupAuthInterceptor } from '@/lib/auth-interceptor';
import { handleQueryError } from '@/lib/handle-query-error';
import { setupNetworkStatusListener } from '@/lib/network-status';
import { ThemeProvider } from '@/providers/theme-provider';

// Preserves the preference persisted by the previous, app-specific theme
// context this replaced, so migrating to BNA UI doesn't reset it.
const THEME_STORAGE_KEY = 'houndjo-mobile-theme';

configureApiClient({ baseURL: apiBaseUrl });
setupAuthInterceptor();
setupNetworkStatusListener();

function RootNavigator() {
  const { isLoading, isAuthenticated } = useAuth();

  if (isLoading) {
    return null;
  }

  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Protected guard={isAuthenticated}>
        <Stack.Screen name="(app)" />
      </Stack.Protected>

      <Stack.Protected guard={!isAuthenticated}>
        <Stack.Screen name="(auth)" />
      </Stack.Protected>
    </Stack>
  );
}

export default function RootLayout() {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        // Screens that render API errors themselves opt out via
        // `meta: { skipGlobalErrorToast: true }` so the user doesn't get the
        // same failure twice, once inline and once as a toast.
        queryCache: new QueryCache({
          onError: (error, query) => {
            if (query.meta?.skipGlobalErrorToast) return;
            handleQueryError(error, { navigateOnForbidden: true });
          },
        }),
        mutationCache: new MutationCache({
          onError: (error, _variables, _context, mutation) => {
            if (mutation.meta?.skipGlobalErrorToast) return;
            handleQueryError(error);
          },
        }),
      })
  );

  useEffect(() => {
    void hydrateAccessToken();
    void hydrateStoredLanguage();
  }, []);

  return (
    <AppErrorBoundary>
      <I18nextProvider i18n={i18n}>
        <QueryClientProvider client={queryClient}>
          <AuthProvider>
            <ThemeProvider storage={AsyncStorage} storageKey={THEME_STORAGE_KEY}>
              <AppTextSizeProvider>
                <AnimatedSplashOverlay />
                <RootNavigator />
                <OfflineBanner />
                <Toaster />
              </AppTextSizeProvider>
            </ThemeProvider>
          </AuthProvider>
        </QueryClientProvider>
      </I18nextProvider>
    </AppErrorBoundary>
  );
}
