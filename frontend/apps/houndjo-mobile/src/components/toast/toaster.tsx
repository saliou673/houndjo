import { useEffect, useState } from "react";
import { StyleSheet, View } from "react-native";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import {
    dismissToast,
    subscribeToasts,
    type ToastItem,
    type ToastVariant,
} from "@/components/toast/toast-store";
import { Toast, type ToastVariant as BnaToastVariant } from "@/components/ui/toast";

const VARIANT_TO_BNA: Record<ToastVariant, BnaToastVariant> = {
    error: "error",
    info: "info",
    success: "success",
};

export function Toaster() {
    const [toasts, setToasts] = useState<ToastItem[]>([]);

    useEffect(() => subscribeToasts(setToasts), []);

    if (toasts.length === 0) {
        return null;
    }

    return (
        <GestureHandlerRootView style={styles.container} pointerEvents="box-none">
            <View pointerEvents="box-none" style={styles.container}>
                {toasts.map((toast, index) => (
                    <Toast
                        key={toast.id}
                        id={String(toast.id)}
                        description={toast.message}
                        variant={VARIANT_TO_BNA[toast.variant]}
                        index={index}
                        onDismiss={(id) => dismissToast(Number(id))}
                    />
                ))}
            </View>
        </GestureHandlerRootView>
    );
}

const styles = StyleSheet.create({
    container: {
        position: "absolute",
        top: 0,
        left: 0,
        right: 0,
        zIndex: 999,
    },
});
