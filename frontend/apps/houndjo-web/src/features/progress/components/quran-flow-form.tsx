"use client";

import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    getProgressStateQueryKey,
    useRecordProgress,
    type ProgressFlowEnumKey,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { createQuranFlowSchema, type QuranFlowForm as QuranFlowValues } from "../data/schema";
import { AssessmentFields } from "./assessment-fields";

type QuranFlowFormProps = {
    flow: ProgressFlowEnumKey;
    courseId: number;
    sessionId: number;
    studentId: number;
};

const DEFAULT_VALUES: QuranFlowValues = {
    fromSurah: 1,
    fromVerse: 1,
    toSurah: 1,
    toVerse: 1,
    errorCount: 0,
    fluency: "GOOD",
    tajweed: undefined,
    note: "",
};

export function QuranFlowForm({ flow, courseId, sessionId, studentId }: QuranFlowFormProps) {
    const t = useTranslations("Progress.entryDialog");
    const tValidation = useTranslations("Progress.entryDialog.validation");
    const queryClient = useQueryClient();
    const formSchema = useMemo(() => createQuranFlowSchema(tValidation), [tValidation]);

    const form = useForm<QuranFlowValues>({
        resolver: zodResolver(formSchema),
        defaultValues: DEFAULT_VALUES,
    });

    const { mutate, isPending } = useRecordProgress({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: getProgressStateQueryKey(studentId, { courseId }),
                });
                toast.success(t("successToast"));
                form.reset(DEFAULT_VALUES);
            },
            onError: handleServerError,
        },
    });

    const onSubmit = (values: QuranFlowValues) => {
        mutate({
            data: {
                studentId,
                courseId,
                sessionId,
                flow,
                fromSurah: values.fromSurah,
                fromVerse: values.fromVerse,
                toSurah: values.toSurah,
                toVerse: values.toVerse,
                errorCount: values.errorCount,
                fluency: values.fluency,
                tajweed: values.tajweed || undefined,
                status: "VALIDATED",
                note: values.note?.trim() || undefined,
            },
        });
    };

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3 pt-2">
                <div className="grid grid-cols-2 gap-3">
                    <FormField
                        control={form.control}
                        name="fromSurah"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.fromSurah")}</FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        min={1}
                                        max={114}
                                        disabled={isPending}
                                        value={field.value ?? ""}
                                        onChange={(event) => field.onChange(Number(event.target.value))}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="fromVerse"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.fromVerse")}</FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        min={1}
                                        disabled={isPending}
                                        value={field.value ?? ""}
                                        onChange={(event) => field.onChange(Number(event.target.value))}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="toSurah"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.toSurah")}</FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        min={1}
                                        max={114}
                                        disabled={isPending}
                                        value={field.value ?? ""}
                                        onChange={(event) => field.onChange(Number(event.target.value))}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="toVerse"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.toVerse")}</FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        min={1}
                                        disabled={isPending}
                                        value={field.value ?? ""}
                                        onChange={(event) => field.onChange(Number(event.target.value))}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                </div>

                <AssessmentFields control={form.control} disabled={isPending} />

                <Button type="submit" disabled={isPending} className="w-full sm:w-auto">
                    {t("submit")}
                </Button>
            </form>
        </Form>
    );
}
