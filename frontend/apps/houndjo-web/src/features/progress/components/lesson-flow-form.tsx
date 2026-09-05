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
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { createLessonFlowSchema, type LessonFlowForm as LessonFlowValues } from "../data/schema";
import { AssessmentFields } from "./assessment-fields";

type LessonFlowFormProps = {
    courseId: number;
    sessionId: number;
    studentId: number;
    lessons: string[];
};

const DEFAULT_VALUES: Omit<LessonFlowValues, "lessonId"> = {
    errorCount: 0,
    fluency: "GOOD",
    tajweed: undefined,
    note: "",
};

export function LessonFlowForm({ courseId, sessionId, studentId, lessons }: LessonFlowFormProps) {
    const t = useTranslations("Progress.entryDialog");
    const tValidation = useTranslations("Progress.entryDialog.validation");
    const queryClient = useQueryClient();
    const formSchema = useMemo(() => createLessonFlowSchema(tValidation), [tValidation]);

    const form = useForm<LessonFlowValues>({
        resolver: zodResolver(formSchema),
        defaultValues: { ...DEFAULT_VALUES, lessonId: 0 },
    });

    const { mutate, isPending } = useRecordProgress({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: getProgressStateQueryKey(studentId, { courseId }),
                });
                toast.success(t("successToast"));
                form.reset({ ...DEFAULT_VALUES, lessonId: 0 });
            },
            onError: handleServerError,
        },
    });

    const onSubmit = (values: LessonFlowValues) => {
        mutate({
            data: {
                studentId,
                courseId,
                sessionId,
                flow: "LESSON",
                lessonId: values.lessonId,
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
                <FormField
                    control={form.control}
                    name="lessonId"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.lesson")}</FormLabel>
                            <Select
                                disabled={isPending}
                                value={String(field.value)}
                                onValueChange={(value) => field.onChange(Number(value))}
                            >
                                <FormControl>
                                    <SelectTrigger className="w-full">
                                        <SelectValue placeholder={t("fields.lessonPlaceholder")} />
                                    </SelectTrigger>
                                </FormControl>
                                <SelectContent>
                                    {lessons.map((lesson, index) => (
                                        <SelectItem key={index} value={String(index)}>
                                            {lesson}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            <FormMessage />
                        </FormItem>
                    )}
                />

                <AssessmentFields control={form.control} disabled={isPending} />

                <Button type="submit" disabled={isPending} className="w-full sm:w-auto">
                    {t("submit")}
                </Button>
            </form>
        </Form>
    );
}
