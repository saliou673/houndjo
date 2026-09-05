"use client";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export function DateRangeFilter({
    id,
    fromDate,
    toDate,
    onFromChange,
    onToChange,
    fromLabel,
    toLabel,
}: {
    id: string;
    fromDate: string;
    toDate: string;
    onFromChange: (value: string) => void;
    onToChange: (value: string) => void;
    fromLabel: string;
    toLabel: string;
}) {
    return (
        <div className="flex flex-wrap items-end gap-3">
            <div className="space-y-1.5">
                <Label htmlFor={`${id}-from`}>{fromLabel}</Label>
                <Input
                    id={`${id}-from`}
                    type="date"
                    value={fromDate}
                    max={toDate || undefined}
                    onChange={(event) => onFromChange(event.target.value)}
                />
            </div>
            <div className="space-y-1.5">
                <Label htmlFor={`${id}-to`}>{toLabel}</Label>
                <Input
                    id={`${id}-to`}
                    type="date"
                    value={toDate}
                    min={fromDate || undefined}
                    onChange={(event) => onToChange(event.target.value)}
                />
            </div>
        </div>
    );
}
