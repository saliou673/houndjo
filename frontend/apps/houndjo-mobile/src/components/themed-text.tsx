import { Platform, StyleSheet, Text, type TextProps } from 'react-native';

import { Fonts, ThemeColor } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { useTextSize } from '@/context/text-size-provider';

export type ThemedTextProps = TextProps & {
  type?: 'default' | 'title' | 'small' | 'smallBold' | 'subtitle' | 'link' | 'linkPrimary' | 'code';
  themeColor?: ThemeColor;
};

// Single source of truth for each type's base size, scaled at render time by
// the user's text size preference. Kept separate from `styles` below so the
// fontWeight/fontFamily/color parts of a type don't need to be duplicated.
const SIZES = {
  small: { fontSize: 14, lineHeight: 20 },
  smallBold: { fontSize: 14, lineHeight: 20 },
  default: { fontSize: 16, lineHeight: 24 },
  title: { fontSize: 48, lineHeight: 52 },
  subtitle: { fontSize: 32, lineHeight: 44 },
  link: { fontSize: 14, lineHeight: 30 },
  linkPrimary: { fontSize: 14, lineHeight: 30 },
  code: { fontSize: 12 },
} as const satisfies Record<NonNullable<ThemedTextProps['type']>, { fontSize: number; lineHeight?: number }>;

export function ThemedText({ style, type = 'default', themeColor, ...rest }: ThemedTextProps) {
  const theme = useTheme();
  const { scale } = useTextSize();

  const base = SIZES[type];
  const scaledSize = {
    fontSize: base.fontSize * scale,
    lineHeight: 'lineHeight' in base ? base.lineHeight * scale : undefined,
  };

  return (
    <Text
      style={[
        { color: theme[themeColor ?? 'text'] },
        type === 'default' && styles.default,
        type === 'title' && styles.title,
        type === 'small' && styles.small,
        type === 'smallBold' && styles.smallBold,
        type === 'subtitle' && styles.subtitle,
        type === 'link' && styles.link,
        type === 'linkPrimary' && styles.linkPrimary,
        type === 'code' && styles.code,
        scaledSize,
        style,
      ]}
      {...rest}
    />
  );
}

const styles = StyleSheet.create({
  small: {
    fontWeight: 500,
  },
  smallBold: {
    fontWeight: 700,
  },
  default: {
    fontWeight: 500,
  },
  title: {
    fontWeight: 600,
  },
  subtitle: {
    fontWeight: 600,
  },
  link: {},
  linkPrimary: {
    color: '#3c87f7',
  },
  code: {
    fontFamily: Fonts.mono,
    fontWeight: Platform.select({ android: 700 }) ?? 500,
  },
});
