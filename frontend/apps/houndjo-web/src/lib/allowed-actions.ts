export function allowedActions<T extends string>(
    permissions: Record<T, boolean>
): ReadonlySet<T> {
    return new Set(
        (Object.keys(permissions) as T[]).filter(
            (action) => permissions[action]
        )
    );
}
