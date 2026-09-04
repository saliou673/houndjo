import { Platform, Pressable, StyleSheet, View } from 'react-native';
import DateTimePicker, {
  DateTimePickerAndroid,
  type DateTimePickerEvent,
} from '@react-native-community/datetimepicker';

import { FormTextField } from '@/components/form-text-field';
import { ThemedText } from '@/components/themed-text';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

// Mirrors the backend's date-only ISO format used across the API (birthDate, sessionDate, ...).
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

function parseDateInput(value: string): Date {
  const match = DATE_PATTERN.exec(value.trim());
  if (!match) return new Date();

  const [year, month, day] = value.trim().split('-').map(Number);
  return new Date(year, month - 1, day);
}

function formatDateValue(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export type DateFieldProps = {
  label: string;
  value: string;
  onChange: (next: string) => void;
  error?: string;
  editable: boolean;
  placeholder: string;
  minimumDate?: Date;
  maximumDate?: Date;
};

export function DateField({
  label,
  value,
  onChange,
  error,
  editable,
  placeholder,
  minimumDate,
  maximumDate,
}: DateFieldProps) {
  const theme = useTheme();

  // No web implementation ships in @react-native-community/datetimepicker
  // (it renders null with a console warning there), so this app's web
  // target keeps the plain text field instead of silently losing the
  // control.
  if (Platform.OS === 'web') {
    return (
      <FormTextField
        label={label}
        value={value}
        onChangeText={onChange}
        placeholder={placeholder}
        error={error}
        autoCapitalize="none"
        autoCorrect={false}
        editable={editable}
      />
    );
  }

  function handleChange(event: DateTimePickerEvent, selectedDate?: Date) {
    if (event.type === 'set' && selectedDate) {
      onChange(formatDateValue(selectedDate));
    }
  }

  if (Platform.OS === 'android') {
    return (
      <View style={styles.field}>
        <ThemedText type="smallBold">{label}</ThemedText>
        <Pressable
          accessibilityRole="button"
          disabled={!editable}
          onPress={() =>
            DateTimePickerAndroid.open({
              value: parseDateInput(value),
              mode: 'date',
              minimumDate,
              maximumDate,
              onChange: handleChange,
            })
          }
          style={[
            styles.dateTrigger,
            {
              backgroundColor: theme.backgroundElement,
              borderColor: error ? theme.danger : theme.backgroundSelected,
            },
          ]}>
          <ThemedText themeColor={value ? 'text' : 'textSecondary'}>{value || placeholder}</ThemedText>
        </Pressable>
        {error && (
          <ThemedText type="small" themeColor="danger">
            {error}
          </ThemedText>
        )}
      </View>
    );
  }

  return (
    <View style={styles.field}>
      <ThemedText type="smallBold">{label}</ThemedText>
      <DateTimePicker
        value={parseDateInput(value)}
        mode="date"
        display="compact"
        minimumDate={minimumDate}
        maximumDate={maximumDate}
        disabled={!editable}
        onChange={handleChange}
        style={styles.dateTriggerIOS}
      />
      {error && (
        <ThemedText type="small" themeColor="danger">
          {error}
        </ThemedText>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  field: {
    gap: Spacing.one,
  },
  dateTrigger: {
    borderWidth: 1,
    borderRadius: Spacing.two,
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    minHeight: 44,
    justifyContent: 'center',
  },
  dateTriggerIOS: {
    alignSelf: 'flex-start',
  },
});
