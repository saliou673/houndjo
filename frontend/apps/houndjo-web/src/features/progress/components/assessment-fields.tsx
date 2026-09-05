"use client";

import { type Control } from "react-hook-form";
import { useTranslations } from "next-intl";
import {
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { RATING_OPTIONS } from "../data/schema";

export type AssessmentValues = {
    errorCount: number;
    fluency?: (typeof RATING_OPTIONS)[number];
    tajweed?: (typeof RATING_OPTIONS)[number];
    note?: string;
};

type AssessmentFieldsProps<T extends AssessmentValues> = {
    control: Control<T>;
    disabled: boolean;
};

export function AssessmentFields<T extends AssessmentValues>({
    control,
    disabled,
}: AssessmentFieldsProps<T>) {
    const t = useTranslations("Progress.entryDialog");

    return (
        <>
            <FormField
                control={control}
                name={"errorCount" as never}
                render={({ field }) => (
                    <FormItem>
                        <FormLabel>{t("fields.errorCount")}</FormLabel>
                        <FormControl>
                            <Input
                                type="number"
                                min={0}
                                disabled={disabled}
                                value={field.value ?? 0}
                                onChange={(event) =>
                                    field.onChange(Number(event.target.value))
                                }
                            />
                        </FormControl>
                        <FormMessage />
                    </FormItem>
                )}
            />
            <div className="grid grid-cols-2 gap-3">
                <FormField
                    control={control}
                    name={"fluency" as never}
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.fluency")}</FormLabel>
                            <Select
                                disabled={disabled}
                                value={field.value}
                                onValueChange={field.onChange}
                            >
                                <FormControl>
                                    <SelectTrigger className="w-full">
                                        <SelectValue
                                            placeholder={t(
                                                "fields.fluencyPlaceholder"
                                            )}
                                        />
                                    </SelectTrigger>
                                </FormControl>
                                <SelectContent>
                                    {RATING_OPTIONS.map((value) => (
                                        <SelectItem
                                            key={value}
                                            value={value}
                                        >
                                            {t(`ratingOptions.${value}`)}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <FormField
                    control={control}
                    name={"tajweed" as never}
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.tajweed")}</FormLabel>
                            <Select
                                disabled={disabled}
                                value={field.value ?? ""}
                                onValueChange={field.onChange}
                            >
                                <FormControl>
                                    <SelectTrigger className="w-full">
                                        <SelectValue
                                            placeholder={t(
                                                "fields.fluencyPlaceholder"
                                            )}
                                        />
                                    </SelectTrigger>
                                </FormControl>
                                <SelectContent>
                                    {RATING_OPTIONS.map((value) => (
                                        <SelectItem
                                            key={value}
                                            value={value}
                                        >
                                            {t(`ratingOptions.${value}`)}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            <FormMessage />
                        </FormItem>
                    )}
                />
            </div>
            <FormField
                control={control}
                name={"note" as never}
                render={({ field }) => (
                    <FormItem>
                        <FormLabel>{t("fields.note")}</FormLabel>
                        <FormControl>
                            <Textarea
                                {...field}
                                disabled={disabled}
                                rows={2}
                            />
                        </FormControl>
                        <FormMessage />
                    </FormItem>
                )}
            />
        </>
    );
}
