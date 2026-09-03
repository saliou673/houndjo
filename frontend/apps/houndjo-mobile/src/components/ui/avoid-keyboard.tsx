import Animated, { useAnimatedKeyboard, useAnimatedStyle } from 'react-native-reanimated';

type Props = { offset?: number };

export const AvoidKeyboard = ({ offset = 0 }: Props) => {
  const keyboard = useAnimatedKeyboard();
  const keyboardMargin = useAnimatedStyle(() => ({
    height:
      keyboard.height.value > 0 ? keyboard.height.value + offset : 0,
  }));

  return <Animated.View style={keyboardMargin} />;
};
