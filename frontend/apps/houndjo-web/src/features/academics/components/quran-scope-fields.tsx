"use client";

import { type Control } from "react-hook-form";
import { useTranslations } from "next-intl";
import { useGetJuz } from "@api-client";
import {
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { type CourseForm } from "../data/course-form-schema";

type QuranScopeFieldsProps = {
    control: Control<CourseForm>;
    disabled?: boolean;
};

export function QuranScopeFields({ control, disabled }: QuranScopeFieldsProps) {
    const t = useTranslations("Classes.courseForm.fields");
    const { data: juzList, isLoading } = useGetJuz();
    const options = juzList ?? [];

    return (
        <div className="grid grid-cols-2 gap-3">
            <FormField
                control={control}
                name="quranScopeFromJuz"
                render={({ field }) => (
                    <FormItem>
                        <FormLabel>{t("quranScopeFromJuz")}</FormLabel>
                        <Select
                            disabled={disabled || isLoading}
                            value={field.value != null ? String(field.value) : undefined}
                            onValueChange={(value) => field.onChange(Number(value))}
                        >
                            <FormControl>
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder={t("quranScopePlaceholder")} />
                                </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                                {options.map((juz) => (
                                    <SelectItem key={juz.number} value={String(juz.number)}>
                                        {t("juzOption", { number: juz.number ?? 0 })}
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
                name="quranScopeToJuz"
                render={({ field }) => (
                    <FormItem>
                        <FormLabel>{t("quranScopeToJuz")}</FormLabel>
                        <Select
                            disabled={disabled || isLoading}
                            value={field.value != null ? String(field.value) : undefined}
                            onValueChange={(value) => field.onChange(Number(value))}
                        >
                            <FormControl>
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder={t("quranScopePlaceholder")} />
                                </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                                {options.map((juz) => (
                                    <SelectItem key={juz.number} value={String(juz.number)}>
                                        {t("juzOption", { number: juz.number ?? 0 })}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                        <FormMessage />
                    </FormItem>
                )}
            />
        </div>
    );
}
