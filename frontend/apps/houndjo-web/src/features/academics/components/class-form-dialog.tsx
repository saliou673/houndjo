"use client";

import { useEffect, useMemo } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import { useCreateClass, useUpdateClass } from "@api-client";
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
import { Textarea } from "@/components/ui/textarea";
import { type ClassRow } from "../data/schema";

function createFormSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        name: z.string().trim().min(1, t("nameRequired")).max(120),
        description: z.string().optional(),
        displayOrder: z.number().int().min(0).optional(),
    });
}

type ClassForm = z.infer<ReturnType<typeof createFormSchema>>;

type ClassFormDialogProps = {
    currentRow?: ClassRow;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

function getDefaultValues(currentRow?: ClassRow): ClassForm {
    return {
        name: currentRow?.name ?? "",
        description: currentRow?.description ?? "",
        displayOrder: currentRow?.displayOrder ?? 0,
    };
}

export function ClassFormDialog({
    currentRow,
    open,
    onOpenChange,
}: ClassFormDialogProps) {
    const t = useTranslations("Classes.form");
    const tValidation = useTranslations("Classes.form.validation");
    const isEdit = !!currentRow;
    const queryClient = useQueryClient();
    const formSchema = useMemo(() => createFormSchema(tValidation), [tValidation]);

    const form = useForm<ClassForm>({
        resolver: zodResolver(formSchema),
        defaultValues: getDefaultValues(currentRow),
    });

    useEffect(() => {
        if (open) {
            form.reset(getDefaultValues(currentRow));
        }
    }, [currentRow, form, open]);

    const invalidateClasses = async () => {
        await queryClient.invalidateQueries({
            queryKey: [{ url: "/api/v1/classes" }],
        });
    };

    const { mutate: createClass, isPending: isCreating } = useCreateClass({
        mutation: {
            onSuccess: async () => {
                await invalidateClasses();
                toast.success(t("toastCreated"));
                form.reset(getDefaultValues());
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const { mutate: updateClass, isPending: isUpdating } = useUpdateClass({
        mutation: {
            onSuccess: async () => {
                await invalidateClasses();
                toast.success(t("toastUpdated"));
                onOpenChange(false);
            },
            onError: handleServerError,
        },
    });

    const isPending = isCreating || isUpdating;

    const onSubmit = (values: ClassForm) => {
        const data = {
            name: values.name.trim(),
            description: values.description?.trim() || undefined,
            displayOrder: values.displayOrder ?? 0,
        };

        if (isEdit) {
            updateClass({ id: currentRow.id, data });
            return;
        }

        createClass({ data });
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
            <DialogContent className="sm:max-w-md">
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
                        id="class-form"
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
                                        <Input
                                            {...field}
                                            disabled={isPending}
                                            placeholder={t("fields.namePlaceholder")}
                                        />
                                    </FormControl>
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
                                        <Textarea
                                            {...field}
                                            disabled={isPending}
                                            placeholder={t("fields.descriptionPlaceholder")}
                                            rows={3}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="displayOrder"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>{t("fields.displayOrder")}</FormLabel>
                                    <FormControl>
                                        <Input
                                            type="number"
                                            min={0}
                                            disabled={isPending}
                                            value={field.value ?? 0}
                                            onChange={(event) =>
                                                field.onChange(
                                                    event.target.value === ""
                                                        ? 0
                                                        : Number(event.target.value)
                                                )
                                            }
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                    </form>
                </Form>
                <DialogFooter>
                    <Button
                        type="submit"
                        form="class-form"
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
