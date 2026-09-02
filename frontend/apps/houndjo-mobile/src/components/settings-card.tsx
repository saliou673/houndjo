import { StyleSheet, type ViewProps } from 'react-native';

import { Card } from '@/components/ui/card';

export function SettingsCard({ style, children, ...props }: ViewProps) {
  return (
    <Card style={StyleSheet.flatten(style)} {...props}>
      {children}
    </Card>
  );
}
