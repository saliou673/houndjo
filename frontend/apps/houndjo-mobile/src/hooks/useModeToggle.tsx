import { Appearance } from 'react-native';

import { Mode, useModeContext } from '@/providers/mode-provider';
import { resolveColorScheme } from '@/theme/color-scheme';

interface UseModeToggleReturn {
  isDark: boolean;
  mode: Mode;
  setMode: (mode: Mode) => void;
  currentMode: 'light' | 'dark';
  toggleMode: () => void;
}

const ignoreModeChange = (_mode: Mode) => {};

/**
 * Reads and writes the app-wide theme mode held by `ModeProvider`.
 *
 * The mode deliberately lives in context rather than in this hook: it used to
 * be local `useState` paired with a global `Appearance.setColorScheme` call, so
 * remounting the toggle reset the cycle to `'system'` while the app stayed
 * dark, and two toggles on screen disagreed. Sharing the state also makes the
 * toggle work on web, where `Appearance` is read-only.
 */
export function useModeToggle(): UseModeToggleReturn {
  const context = useModeContext();
  const { mode, setMode, scheme } = context ?? {
    mode: 'system',
    setMode: ignoreModeChange,
    scheme: resolveColorScheme(Appearance.getColorScheme()),
  };

  const toggleMode = () => {
    switch (mode) {
      case 'light':
        setMode('dark');
        break;
      case 'dark':
        setMode('system');
        break;
      case 'system':
        setMode('light');
        break;
    }
  };

  return {
    isDark: scheme === 'dark',
    mode,
    setMode,
    currentMode: scheme,
    toggleMode,
  };
}
