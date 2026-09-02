import { Component, type ReactNode } from "react";
import { Pressable, StyleSheet } from "react-native";
import i18n from "@/i18n";
import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Spacing } from "@/constants/theme";

type AppErrorBoundaryProps = {
    children: ReactNode;
};

type AppErrorBoundaryState = {
    hasError: boolean;
};

export class AppErrorBoundary extends Component<
    AppErrorBoundaryProps,
    AppErrorBoundaryState
> {
    state: AppErrorBoundaryState = { hasError: false };

    static getDerivedStateFromError(): AppErrorBoundaryState {
        return { hasError: true };
    }

    componentDidCatch(error: unknown) {
        if (__DEV__) {
            console.error(error);
        }
    }

    reset = () => {
        this.setState({ hasError: false });
    };

    render() {
        if (!this.state.hasError) {
            return this.props.children;
        }

        return (
            <ThemedView style={styles.container}>
                <ThemedText type="title" style={styles.text}>
                    {i18n.t("errors.boundaryTitle")}
                </ThemedText>
                <ThemedText
                    type="small"
                    themeColor="textSecondary"
                    style={styles.text}
                >
                    {i18n.t("errors.boundaryHint")}
                </ThemedText>
                <Pressable
                    style={({ pressed }) => [
                        styles.retryButton,
                        pressed && styles.pressed,
                    ]}
                    onPress={this.reset}
                >
                    <ThemedText type="link">
                        {i18n.t("errors.boundaryRetry")}
                    </ThemedText>
                </Pressable>
            </ThemedView>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        gap: Spacing.three,
        paddingHorizontal: Spacing.four,
    },
    text: {
        textAlign: "center",
    },
    retryButton: {
        marginTop: Spacing.four,
        paddingHorizontal: Spacing.four,
        paddingVertical: Spacing.two,
        borderRadius: Spacing.five,
    },
    pressed: {
        opacity: 0.7,
    },
});
