"use client";

import { useEffect, useMemo } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    useCreateStudent,
    useUpdateStudent,
    type StudentGenderEnumKey,
} from "@api-client";
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
import { type StudentRow } from "../data/schema";

const GENDERS: StudentGenderEnumKey[] = ["MALE", "FEMALE"];

function createFormSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        firstName: z.string().trim().min(1, t("firstNameRequired")).max(255),
        lastName: z.string().trim().min(1, t("lastNameRequired")).max(255),
        birthDate: z.string().optional(),
        gender: z.enum(["MALE", "FEMALE"]).optional(),
        guardianName: z.string().max(255).optional(),
        guardianPhone: z.string().max(20).optional(),
    });
}

type StudentForm = z.infer<ReturnType<typeof createFormSchema>>;

type StudentFormDialogProps = {
    currentRow?: StudentRow;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

function getDefaultValues(currentRow?: StudentRow): StudentForm {
    return {
        firstName: currentRow?.firstName ?? "",
        lastName: currentRow?.lastName ?? "",
        birthDate: currentRow?.birthDate ?? "",
        gender: currentRow?.gender ?? undefined,
        guardianName: currentRow?.guardianName ?? "",
        guardianPhone: currentRow?.guardianPhone ?? "",
    };
}

export function StudentFormDialog({
    currentRow,
    open,
    onOpenChange,
}: StudentFormDialogProps) {
    const t = useTranslations("Students.form");
    const tValidation = useTranslations("Students.form.validation");
    const isEdit = !!currentRow;
    const queryClient = useQueryClient();
    const formSchema = useMemo(() => createFormSchema(tValidation), [tValidation]);

    const form = useForm<StudentForm>({
        resolver: zodResolver(formSchema),
        defaultValues: getDefaultValues(currentRow),
    });

    useEffect(() => {
        if (open) {
            form.reset(getDefaultValues(currentRow));
        }
    }, [currentRow, form, open]);

    const invalidateStudents = async () => {
        await queryClient.invalidateQueries({
            queryKey: [{ url: "/api/v1/students" }],
        });
    };

    const { mutate: createStudent, isPending: isCreating } = useCreateStudent({
        mutation: {
            onSuccess: async () => {
                await invalidateStudents();
                toast.success(t("toastCreated"));
                form.reset(getDefaultValues());
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const { mutate: updateStudent, isPending: isUpdating } = useUpdateStudent({
        mutation: {
            onSuccess: async () => {
                await invalidateStudents();
                toast.success(t("toastUpdated"));
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const isPending = isCreating || isUpdating;

    const onSubmit = (values: StudentForm) => {
        const data = {
            firstName: values.firstName.trim(),
            lastName: values.lastName.trim(),
            birthDate: values.birthDate || undefined,
            gender: values.gender,
            guardianName: values.guardianName?.trim() || undefined,
            guardianPhone: values.guardianPhone?.trim() || undefined,
        };

        if (isEdit) {
            updateStudent({ id: currentRow.id, data });
            return;
        }

        createStudent({ data });
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
                    <DialogTitle>
                        {isEdit ? t("editTitle") : t("addTitle")}
                    </DialogTitle>
                    <DialogDescription>
                        {isEdit ? t("editDescription") : t("addDescription")}
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form
                        id="student-form"
                        onSubmit={form.handleSubmit(onSubmit)}
                        className="space-y-4"
                    >
                        <div className="grid grid-cols-2 gap-3">
                            <FormField
                                control={form.control}
                                name="firstName"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>
                                            {t("fields.firstName")}
                                        </FormLabel>
                                        <FormControl>
                                            <Input
                                                {...field}
                                                disabled={isPending}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="lastName"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>
                                            {t("fields.lastName")}
                                        </FormLabel>
                                        <FormControl>
                                            <Input
                                                {...field}
                                                disabled={isPending}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-3">
                            <FormField
                                control={form.control}
                                name="birthDate"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>
                                            {t("fields.birthDate")}
                                        </FormLabel>
                                        <FormControl>
                                            <Input
                                                type="date"
                                                {...field}
                                                disabled={isPending}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="gender"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>
                                            {t("fields.gender")}
                                        </FormLabel>
                                        <Select
                                            disabled={isPending}
                                            value={field.value}
                                            onValueChange={field.onChange}
                                        >
                                            <FormControl>
                                                <SelectTrigger className="w-full">
                                                    <SelectValue
                                                        placeholder={t(
                                                            "fields.genderPlaceholder"
                                                        )}
                                                    />
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                {GENDERS.map((value) => (
                                                    <SelectItem
                                                        key={value}
                                                        value={value}
                                                    >
                                                        {t(
                                                            `genderOptions.${value}`
                                                        )}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-3">
                            <FormField
                                control={form.control}
                                name="guardianName"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>
                                            {t("fields.guardianName")}
                                        </FormLabel>
                                        <FormControl>
                                            <Input
                                                {...field}
                                                disabled={isPending}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="guardianPhone"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>
                                            {t("fields.guardianPhone")}
                                        </FormLabel>
                                        <FormControl>
                                            <Input
                                                {...field}
                                                disabled={isPending}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>
                    </form>
                </Form>
                <DialogFooter>
                    <Button
                        type="submit"
                        form="student-form"
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
