"use client";

import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import { getProgressStateQueryKey, useRecordProgress } from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { createChapterFlowSchema, type ChapterFlowForm as ChapterFlowValues } from "../data/schema";
import { AssessmentFields } from "./assessment-fields";

type ChapterFlowFormProps = {
    courseId: number;
    sessionId: number;
    studentId: number;
};

const DEFAULT_VALUES: ChapterFlowValues = {
    chapterNo: 1,
    pageNo: 1,
    errorCount: 0,
    fluency: "GOOD",
    tajweed: undefined,
    note: "",
};

export function ChapterFlowForm({ courseId, sessionId, studentId }: ChapterFlowFormProps) {
    const t = useTranslations("Progress.entryDialog");
    const tValidation = useTranslations("Progress.entryDialog.validation");
    const queryClient = useQueryClient();
    const formSchema = useMemo(() => createChapterFlowSchema(tValidation), [tValidation]);

    const form = useForm<ChapterFlowValues>({
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

    const onSubmit = (values: ChapterFlowValues) => {
        mutate({
            data: {
                studentId,
                courseId,
                sessionId,
                flow: "CHAPTER",
                chapterNo: values.chapterNo,
                pageNo: values.pageNo,
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
                        name="chapterNo"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.chapterNo")}</FormLabel>
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
                        name="pageNo"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("fields.pageNo")}</FormLabel>
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
