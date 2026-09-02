import { useEffect, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import Animated, { FadeInUp, FadeOutUp } from "react-native-reanimated";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import {
    dismissToast,
    subscribeToasts,
    type ToastItem,
    type ToastVariant,
} from "@/components/toast/toast-store";

const VARIANT_BACKGROUND: Record<ToastVariant, string> = {
    error: "#DC2626",
    info: "#1F2937",
    success: "#16A34A",
};

export function Toaster() {
    const [toasts, setToasts] = useState<ToastItem[]>([]);
    const insets = useSafeAreaInsets();

    useEffect(() => subscribeToasts(setToasts), []);

    if (toasts.length === 0) {
        return null;
    }

    return (
        <View
            pointerEvents="box-none"
            style={[styles.container, { top: insets.top + 8 }]}
        >
            {toasts.map((toast) => (
                <Animated.View
                    key={toast.id}
                    entering={FadeInUp}
                    exiting={FadeOutUp}
                    style={[
                        styles.toast,
                        { backgroundColor: VARIANT_BACKGROUND[toast.variant] },
                    ]}
                >
                    <Pressable onPress={() => dismissToast(toast.id)}>
                        <Text style={styles.text}>{toast.message}</Text>
                    </Pressable>
                </Animated.View>
            ))}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        position: "absolute",
        left: 16,
        right: 16,
        gap: 8,
        zIndex: 999,
    },
    toast: {
        borderRadius: 10,
        paddingHorizontal: 16,
        paddingVertical: 12,
        shadowColor: "#000000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.2,
        shadowRadius: 4,
        elevation: 4,
    },
    text: {
        color: "#ffffff",
        fontSize: 14,
        fontWeight: "600",
    },
});
