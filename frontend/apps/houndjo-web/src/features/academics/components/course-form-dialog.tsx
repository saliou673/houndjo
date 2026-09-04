"use client";

import { useEffect, useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import { useCreateCourse, useUpdateCourse, type CourseTypeEnumKey } from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {
    Form,
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
import { createCourseFormSchema, type CourseForm } from "../data/course-form-schema";
import { type CourseRow } from "../data/schema";
import { QuranScopeFields } from "./quran-scope-fields";

const COURSE_TYPES: CourseTypeEnumKey[] = ["QAIDA", "QURAN", "BOOK"];

type CourseFormDialogProps = {
    classId: number;
    currentRow?: CourseRow;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

function getDefaultValues(currentRow?: CourseRow): CourseForm {
    return {
        name: currentRow?.name ?? "",
        type: currentRow?.type ?? "QAIDA",
        description: currentRow?.description ?? "",
        quranMode: currentRow?.quranMode ?? undefined,
        quranScopeFromJuz: currentRow?.quranScopeFromJuz ?? undefined,
        quranScopeToJuz: currentRow?.quranScopeToJuz ?? undefined,
        bookTitle: currentRow?.bookTitle ?? "",
        bookTotalChapters: currentRow?.bookTotalChapters ?? undefined,
        bookTotalPages: currentRow?.bookTotalPages ?? undefined,
    };
}

export function CourseFormDialog({
    classId,
    currentRow,
    open,
    onOpenChange,
}: CourseFormDialogProps) {
    const t = useTranslations("Classes.courseForm");
    const tValidation = useTranslations("Classes.courseForm.validation");
    const isEdit = !!currentRow;
    const queryClient = useQueryClient();
    const formSchema = useMemo(() => createCourseFormSchema(tValidation), [tValidation]);

    const form = useForm<CourseForm>({
        resolver: zodResolver(formSchema),
        defaultValues: getDefaultValues(currentRow),
    });

    const type = form.watch("type");

    useEffect(() => {
        if (open) {
            form.reset(getDefaultValues(currentRow));
        }
    }, [currentRow, form, open]);

    const invalidateCourses = async () => {
        await queryClient.invalidateQueries({
            queryKey: [{ url: "/api/v1/classes/:classId/courses", params: { classId } }],
        });
    };

    const { mutate: createCourse, isPending: isCreating } = useCreateCourse({
        mutation: {
            onSuccess: async () => {
                await invalidateCourses();
                toast.success(t("toastCreated"));
                form.reset(getDefaultValues());
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const { mutate: updateCourse, isPending: isUpdating } = useUpdateCourse({
        mutation: {
            onSuccess: async () => {
                await invalidateCourses();
                toast.success(t("toastUpdated"));
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const isPending = isCreating || isUpdating;

    const onSubmit = (values: CourseForm) => {
        const data = {
            name: values.name.trim(),
            type: values.type,
            description: values.description?.trim() || undefined,
            quranMode: values.type === "QURAN" ? values.quranMode : undefined,
            quranScopeFromJuz: values.type === "QURAN" ? values.quranScopeFromJuz : undefined,
            quranScopeToJuz: values.type === "QURAN" ? values.quranScopeToJuz : undefined,
            bookTitle: values.type === "BOOK" ? values.bookTitle?.trim() : undefined,
            bookTotalChapters: values.type === "BOOK" ? values.bookTotalChapters : undefined,
            bookTotalPages: values.type === "BOOK" ? values.bookTotalPages : undefined,
        };

        if (isEdit) {
            updateCourse({ classId, id: currentRow.id, data });
            return;
        }

        createCourse({ classId, data });
    };

    return (
        <Dialog
            open={open}
            onOpenChange={(nextOpen) => {
                if (!isPending) {
                    if (!nextOpen) {
                        form.reset(getDefaultValues(currentRow));
                    }
                    onOpenChange(nextOpen);
                }
            }}
        >
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-lg">
                <DialogHeader className="text-start">
                    <DialogTitle>{isEdit ? t("editTitle") : t("addTitle")}</DialogTitle>
                    <DialogDescription>
                        {isEdit ? t("editDescription") : t("addDescription")}
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form
                        id="course-form"
                        onSubmit={form.handleSubmit(onSubmit)}
                        className="space-y-4"
                    >
                        <FormField
                            control={form.control}
                            name="name"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>{t("fields.name")}</FormLabel>
                                    <FormControl>
                                        <Input {...field} disabled={isPending} />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="type"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>{t("fields.type")}</FormLabel>
                                    <Select
                                        disabled={isPending}
                                        value={field.value}
                                        onValueChange={field.onChange}
                                    >
                                        <FormControl>
                                            <SelectTrigger className="w-full">
                                                <SelectValue />
                                            </SelectTrigger>
                                        </FormControl>
                                        <SelectContent>
                                            {COURSE_TYPES.map((value) => (
                                                <SelectItem key={value} value={value}>
                                                    {t(`typeOptions.${value}`)}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="description"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>{t("fields.description")}</FormLabel>
                                    <FormControl>
                                        <Textarea {...field} disabled={isPending} rows={3} />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />

                        {type === "QURAN" && (
                            <div className="space-y-4 rounded-md border p-3">
                                <FormField
                                    control={form.control}
                                    name="quranMode"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>{t("fields.quranMode")}</FormLabel>
                                            <Select
                                                disabled={isPending}
                                                value={field.value}
                                                onValueChange={field.onChange}
                                            >
                                                <FormControl>
                                                    <SelectTrigger className="w-full">
                                                        <SelectValue
                                                            placeholder={t("fields.quranModePlaceholder")}
                                                        />
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value="NAZIRA">
                                                        {t("quranModeOptions.NAZIRA")}
                                                    </SelectItem>
                                                    <SelectItem value="HIFZ">
                                                        {t("quranModeOptions.HIFZ")}
                                                    </SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <QuranScopeFields control={form.control} disabled={isPending} />
                            </div>
                        )}

                        {type === "BOOK" && (
                            <div className="space-y-4 rounded-md border p-3">
                                <FormField
                                    control={form.control}
                                    name="bookTitle"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>{t("fields.bookTitle")}</FormLabel>
                                            <FormControl>
                                                <Input {...field} disabled={isPending} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <div className="grid grid-cols-2 gap-3">
                                    <FormField
                                        control={form.control}
                                        name="bookTotalChapters"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>{t("fields.bookTotalChapters")}</FormLabel>
                                                <FormControl>
                                                    <Input
                                                        type="number"
                                                        min={1}
                                                        disabled={isPending}
                                                        value={field.value ?? ""}
                                                        onChange={(event) =>
                                                            field.onChange(
                                                                event.target.value === ""
                                                                    ? undefined
                                                                    : Number(event.target.value)
                                                            )
                                                        }
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={form.control}
                                        name="bookTotalPages"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>{t("fields.bookTotalPages")}</FormLabel>
                                                <FormControl>
                                                    <Input
                                                        type="number"
                                                        min={1}
                                                        disabled={isPending}
                                                        value={field.value ?? ""}
                                                        onChange={(event) =>
                                                            field.onChange(
                                                                event.target.value === ""
                                                                    ? undefined
                                                                    : Number(event.target.value)
                                                            )
                                                        }
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>
                            </div>
                        )}
                    </form>
                </Form>
                <DialogFooter className="mt-2 border-t pt-4">
                    <Button
                        type="submit"
                        form="course-form"
                        disabled={isPending}
                        className="w-full sm:w-auto"
                    >
                        {isEdit ? t("submitEdit") : t("submitAdd")}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
