import type { AttendanceStatusEnumKey } from "@api-client";

export function hasValidId<T extends { id?: number }>(
    value: T
): value is T & { id: number } {
    return Number.isSafeInteger(value.id) && Number(value.id) > 0;
}
export const attendanceAppearance: Record<
    AttendanceStatusEnumKey,
    {
        variant: "default" | "secondary" | "destructive" | "outline";
        className: string;
    }
> = {
    PRESENT: {
        variant: "default",
        className: "bg-emerald-600 text-white hover:bg-emerald-600/90",
    },
    ABSENT_JUSTIFIED: {
        variant: "secondary",
        className:
            "bg-secondary text-secondary-foreground hover:bg-secondary/90",
    },
    ABSENT_UNJUSTIFIED: {
        variant: "destructive",
        className:
            "bg-destructive text-destructive-foreground hover:bg-destructive/90",
    },
    PERMISSION: {
        variant: "outline",
        className:
            "border-primary bg-primary/10 text-primary hover:bg-primary/20",
    },
};
