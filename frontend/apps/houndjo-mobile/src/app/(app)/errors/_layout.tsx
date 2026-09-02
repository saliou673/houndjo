import { Stack } from 'expo-router';

import { useTheme } from '@/hooks/use-theme';

export default function ErrorsLayout() {
  const theme = useTheme();

  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: theme.background },
        headerTintColor: theme.text,
        headerShadowVisible: false,
        contentStyle: { backgroundColor: theme.background },
      }}
    />
  );
}
