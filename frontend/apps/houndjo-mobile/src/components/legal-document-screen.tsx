import { Linking, Pressable, StyleSheet, View } from 'react-native';

import { SettingsListScreen } from '@/components/settings-list-screen';
import { ThemedText } from '@/components/themed-text';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export type LegalSection = {
  heading: string;
  body: string;
  list?: string[];
};

type LegalDocumentScreenProps = {
  description: string;
  sections: LegalSection[];
  contactEmail?: string;
};

export function LegalDocumentScreen({ description, sections, contactEmail }: LegalDocumentScreenProps) {
  const theme = useTheme();

  return (
    <SettingsListScreen description={description}>
      <View style={styles.sections}>
        {sections.map((section) => (
          <View key={section.heading} style={styles.section}>
            <ThemedText type="smallBold" style={styles.heading}>
              {section.heading}
            </ThemedText>
            <ThemedText type="small" themeColor="textSecondary">
              {section.body}
            </ThemedText>
            {section.list && (
              <View style={styles.list}>
                {section.list.map((item) => (
                  <View key={item} style={styles.listItem}>
                    <ThemedText type="small" themeColor="textSecondary">
                      {'•'}
                    </ThemedText>
                    <ThemedText type="small" themeColor="textSecondary" style={styles.listItemText}>
                      {item}
                    </ThemedText>
                  </View>
                ))}
              </View>
            )}
          </View>
        ))}

        {contactEmail && (
          <Pressable
            accessibilityRole="link"
            onPress={() => void Linking.openURL(`mailto:${contactEmail}`)}
            style={({ pressed }) => pressed && styles.pressed}>
            <ThemedText type="linkPrimary" style={{ color: theme.text }}>
              {contactEmail}
            </ThemedText>
          </Pressable>
        )}
      </View>
    </SettingsListScreen>
  );
}

const styles = StyleSheet.create({
  sections: {
    gap: Spacing.four,
  },
  section: {
    gap: Spacing.two,
  },
  heading: {
    fontSize: 18,
  },
  list: {
    gap: Spacing.one,
  },
  listItem: {
    flexDirection: 'row',
    gap: Spacing.two,
  },
  listItemText: {
    flex: 1,
  },
  pressed: {
    opacity: 0.7,
  },
});
