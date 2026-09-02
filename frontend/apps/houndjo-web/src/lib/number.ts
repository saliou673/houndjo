export function toNumericOrUndefined(value?: string): number | undefined {
    if (!value?.trim()) return undefined;
    return Number(value);
}

export function toIntOrUndefined(value?: string): number | undefined {
    if (!value?.trim()) return undefined;
    return Math.round(Number(value));
}
