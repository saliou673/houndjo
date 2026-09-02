// https://docs.expo.dev/guides/using-eslint/
const { defineConfig } = require('eslint/config');
const expoConfig = require("eslint-config-expo/flat");

module.exports = defineConfig([
  expoConfig,
  {
    ignores: ["dist/*"],
  },
  {
    rules: {
      // This app's edit/detail screens deliberately copy an already-fetched
      // entity (from a React Query hook) into local editable form state via
      // useEffect + setState once it arrives - a correct, common pattern,
      // not the "derived state" anti-pattern this rule targets. Downgraded
      // rather than reworked across every screen that follows it (including
      // Expo's own generated src/hooks/use-color-scheme.web.ts).
      "react-hooks/set-state-in-effect": "warn",
    },
  },
]);
