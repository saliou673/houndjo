export type ToastVariant = "error" | "info" | "success";

export type ToastItem = {
    id: number;
    message: string;
    variant: ToastVariant;
};

const DEFAULT_DURATION_MS = 4000;

let toasts: ToastItem[] = [];
let listeners: ((items: ToastItem[]) => void)[] = [];
let nextId = 0;

function emit() {
    listeners.forEach((listener) => listener(toasts));
}

export function subscribeToasts(
    listener: (items: ToastItem[]) => void
): () => void {
    listeners.push(listener);
    listener(toasts);

    return () => {
        listeners = listeners.filter((candidate) => candidate !== listener);
    };
}

export function dismissToast(id: number) {
    toasts = toasts.filter((toast) => toast.id !== id);
    emit();
}

export function showToast(
    message: string,
    variant: ToastVariant = "error",
    durationMs = DEFAULT_DURATION_MS
) {
    const id = nextId++;
    toasts = [...toasts, { id, message, variant }];
    emit();

    setTimeout(() => dismissToast(id), durationMs);

    return id;
}
