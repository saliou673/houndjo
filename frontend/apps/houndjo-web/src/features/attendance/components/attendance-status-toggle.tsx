"use client";

import { type AttendanceStatusEnumKey } from "@api-client";
import { useTranslations } from "next-intl";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { ATTENDANCE_STATUSES } from "../data/schema";

type AttendanceStatusToggleProps = {
    value: AttendanceStatusEnumKey;
    onChange: (status: AttendanceStatusEnumKey) => void;
    disabled?: boolean;
};

export function AttendanceStatusToggle({
    value,
    onChange,
    disabled,
}: AttendanceStatusToggleProps) {
    const t = useTranslations("Attendance.statusOptionsShort");

    return (
        <div className="flex flex-wrap gap-1">
            {ATTENDANCE_STATUSES.map((status) => (
                <Button
                    key={status}
                    type="button"
                    size="sm"
                    variant={status === value ? "default" : "outline"}
                    disabled={disabled}
                    onClick={() => onChange(status)}
                    className={cn(
                        "h-8 px-2 text-xs",
                        status === "PRESENT" &&
                            status === value &&
                            "bg-emerald-600 hover:bg-emerald-600/90",
                        status === "ABSENT_UNJUSTIFIED" &&
                            status === value &&
                            "bg-destructive hover:bg-destructive/90"
                    )}
                >
                    {t(status)}
                </Button>
            ))}
        </div>
    );
}
