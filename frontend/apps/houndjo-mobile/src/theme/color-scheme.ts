import type { ColorSchemeName } from 'react-native';

export type ResolvedColorScheme = 'light' | 'dark';

/** Keep React Native's nullable/unspecified scheme at one binary theme boundary. */
export function resolveColorScheme(
  scheme: ColorSchemeName | null | undefined
): ResolvedColorScheme {
  return scheme === 'dark' ? 'dark' : 'light';
}
