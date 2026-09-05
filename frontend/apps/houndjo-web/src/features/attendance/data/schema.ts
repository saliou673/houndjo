import { z } from "zod";
import { type AttendanceStatusEnumKey } from "@api-client";

export const ATTENDANCE_STATUSES: AttendanceStatusEnumKey[] = [
    "PRESENT",
    "ABSENT_JUSTIFIED",
    "ABSENT_UNJUSTIFIED",
    "PERMISSION",
];

export type AttendanceEntryState = {
    status: AttendanceStatusEnumKey;
    reason: string;
};

export function createAttendancePermissionSchema(t: (key: string) => string) {
    return z
        .object({
            fromDate: z.string().min(1, t("fromDateRequired")),
            toDate: z.string().min(1, t("toDateRequired")),
            reason: z.string().max(255, t("reasonTooLong")).optional(),
        })
        .refine((data) => data.fromDate <= data.toDate, {
            message: t("dateRangeInvalid"),
            path: ["toDate"],
        });
}

export type AttendancePermissionForm = z.infer<
    ReturnType<typeof createAttendancePermissionSchema>
>;

export function createRollCallSchema(t: (key: string) => string) {
    return z.object({
        entries: z
            .record(
                z.string(),
                z.object({
                    studentId: z.number().int().positive(),
                    status: z.enum([
                        "PRESENT",
                        "ABSENT_JUSTIFIED",
                        "ABSENT_UNJUSTIFIED",
                        "PERMISSION",
                    ]),
                    reason: z.string().max(255, t("reasonTooLong")),
                })
            )
            .refine((entries) => Object.keys(entries).length > 0),
    });
}
export type RollCallValues = z.infer<ReturnType<typeof createRollCallSchema>>;
