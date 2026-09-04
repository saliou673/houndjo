"use client";

import { useEffect, useMemo } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import { useCreateEnrollment, useGetClasses, useGetCourses, useGetStudents } from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
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
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";

const LIST_PAGEABLE = { page: 0, size: 100 };

function createFormSchema(t: ReturnType<typeof useTranslations>) {
    return z
        .object({
            studentId: z.number().optional(),
            classId: z.number().optional(),
            courseIds: z.array(z.number()),
        })
        .superRefine((values, ctx) => {
            if (values.studentId == null) {
                ctx.addIssue({
                    code: z.ZodIssueCode.custom,
                    message: t("studentRequired"),
                    path: ["studentId"],
                });
            }
            if (values.classId == null) {
                ctx.addIssue({
                    code: z.ZodIssueCode.custom,
                    message: t("classRequired"),
                    path: ["classId"],
                });
            }
        });
}

type EnrollmentForm = z.infer<ReturnType<typeof createFormSchema>>;

type EnrollmentFormDialogProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

export function EnrollmentFormDialog({
    open,
    onOpenChange,
}: EnrollmentFormDialogProps) {
    const t = useTranslations("Enrollments.form");
    const tValidation = useTranslations("Enrollments.form.validation");
    const queryClient = useQueryClient();
    const formSchema = useMemo(() => createFormSchema(tValidation), [tValidation]);

    const form = useForm<EnrollmentForm>({
        resolver: zodResolver(formSchema),
        defaultValues: { studentId: undefined, classId: undefined, courseIds: [] },
    });

    useEffect(() => {
        if (open) {
            form.reset({ studentId: undefined, classId: undefined, courseIds: [] });
        }
    }, [open, form]);

    const classId = form.watch("classId");
    const { data: studentsData } = useGetStudents({ pageable: LIST_PAGEABLE });
    const { data: classesData } = useGetClasses({ pageable: LIST_PAGEABLE });
    const { data: coursesData } = useGetCourses(classId ?? 0, {
        pageable: LIST_PAGEABLE,
    });

    const students = studentsData?.items ?? [];
    const classes = classesData?.items ?? [];
    const courses = classId ? (coursesData?.items ?? []) : [];

    const invalidateEnrollments = async () => {
        await queryClient.invalidateQueries({
            queryKey: [{ url: "/api/v1/enrollments" }],
        });
    };

    const { mutate: createEnrollment, isPending } = useCreateEnrollment({
        mutation: {
            onSuccess: async () => {
                await invalidateEnrollments();
                toast.success(t("toastCreated"));
                form.reset({ studentId: undefined, classId: undefined, courseIds: [] });
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const onSubmit = (values: EnrollmentForm) => {
        if (values.studentId == null || values.classId == null) {
            return;
        }
        createEnrollment({
            data: {
                studentId: values.studentId,
                classId: values.classId,
                courseIds: values.courseIds,
            },
        });
    };

    return (
        <Dialog
            open={open}
            onOpenChange={(nextOpen) => {
                if (!isPending) {
                    onOpenChange(nextOpen);
                }
            }}
        >
            <DialogContent className="flex max-h-[90vh] flex-col overflow-y-auto sm:max-w-lg">
                <DialogHeader className="text-start">
                    <DialogTitle>{t("addTitle")}</DialogTitle>
                    <DialogDescription>{t("addDescription")}</DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form
                        id="enrollment-form"
                        onSubmit={form.handleSubmit(onSubmit)}
                        className="space-y-4"
                    >
                        <FormField
                            control={form.control}
                            name="studentId"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>{t("fields.student")}</FormLabel>
                                    <Select
                                        disabled={isPending}
                                        value={field.value != null ? String(field.value) : undefined}
                                        onValueChange={(value) => field.onChange(Number(value))}
                                    >
                                        <FormControl>
                                            <SelectTrigger className="w-full">
                                                <SelectValue placeholder={t("fields.studentPlaceholder")} />
                                            </SelectTrigger>
                                        </FormControl>
                                        <SelectContent>
                                            {students.map((student) => (
                                                <SelectItem
                                                    key={student.id}
                                                    value={String(student.id)}
                                                >
                                                    {student.firstName} {student.lastName}
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
                            name="classId"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>{t("fields.class")}</FormLabel>
                                    <Select
                                        disabled={isPending}
                                        value={field.value != null ? String(field.value) : undefined}
                                        onValueChange={(value) => {
                                            field.onChange(Number(value));
                                            form.setValue("courseIds", []);
                                        }}
                                    >
                                        <FormControl>
                                            <SelectTrigger className="w-full">
                                                <SelectValue placeholder={t("fields.classPlaceholder")} />
                                            </SelectTrigger>
                                        </FormControl>
                                        <SelectContent>
                                            {classes.map((schoolClass) => (
                                                <SelectItem
                                                    key={schoolClass.id}
                                                    value={String(schoolClass.id)}
                                                >
                                                    {schoolClass.name}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        {classId != null && (
                            <FormField
                                control={form.control}
                                name="courseIds"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>{t("fields.courses")}</FormLabel>
                                        <div className="space-y-2 rounded-md border p-3">
                                            {courses.length === 0 && (
                                                <p className="text-sm text-muted-foreground">
                                                    {t("fields.noCourses")}
                                                </p>
                                            )}
                                            {courses.map((course) => {
                                                const checked = field.value.includes(
                                                    course.id ?? 0
                                                );
                                                return (
                                                    <div
                                                        key={course.id}
                                                        className="flex items-center gap-2"
                                                    >
                                                        <Checkbox
                                                            id={`course-${course.id}`}
                                                            checked={checked}
                                                            disabled={isPending}
                                                            onCheckedChange={(value) => {
                                                                const id = course.id ?? 0;
                                                                field.onChange(
                                                                    value
                                                                        ? [...field.value, id]
                                                                        : field.value.filter(
                                                                              (courseId) =>
                                                                                  courseId !== id
                                                                          )
                                                                );
                                                            }}
                                                        />
                                                        <Label htmlFor={`course-${course.id}`}>
                                                            {course.name}
                                                        </Label>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        )}
                    </form>
                </Form>
                <DialogFooter>
                    <Button
                        type="submit"
                        form="enrollment-form"
                        disabled={isPending}
                        className="w-full sm:w-auto"
                    >
                        {t("submitAdd")}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
