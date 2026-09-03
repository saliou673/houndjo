import { useColor } from '@/hooks/useColor';
import { LucideProps } from 'lucide-react-native';
import React from 'react';

export type Props = LucideProps & {
  lightColor?: string;
  darkColor?: string;
  name: React.ComponentType<LucideProps>;
};

export function Icon({
  lightColor,
  darkColor,
  name: IconComponent,
  color,
  size = 24,
  strokeWidth = 1.8,
  accessible,
  ...rest
}: Props) {
  const themedColor = useColor('icon', { light: lightColor, dark: darkColor });

  // Use provided color prop if available, otherwise use themed color
  const iconColor = color || themedColor;

  return (
    <IconComponent
      color={iconColor}
      size={size}
      strokeWidth={strokeWidth}
      strokeLinecap='round'
      // Only forward `accessible` when the caller sets it explicitly: on web,
      // react-native-svg passes unknown props straight through to the DOM
      // <svg>, and `accessible={false}` there isn't a valid HTML attribute -
      // React logs a warning for it on every render.
      {...(accessible !== undefined ? { accessible } : {})}
      {...rest}
    />
  );
}
