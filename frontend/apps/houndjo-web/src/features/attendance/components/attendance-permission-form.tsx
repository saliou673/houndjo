"use client";
import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    useCreateAttendancePermission,
    getAttendancePermissionsByStudentQueryKey,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
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
import {
    createAttendancePermissionSchema,
    type AttendancePermissionForm as Values,
} from "../data/schema";

const DEFAULT_VALUES: Values = { fromDate: "", toDate: "", reason: "" };
export function AttendancePermissionForm({ studentId }: { studentId: number }) {
    const t = useTranslations("Attendance.studentDialog");
    const tValidation = useTranslations("Attendance.studentDialog.validation");
    const queryClient = useQueryClient();
    const formSchema = useMemo(
        () => createAttendancePermissionSchema(tValidation),
        [tValidation]
    );
    const form = useForm<Values>({
        resolver: zodResolver(formSchema),
        defaultValues: DEFAULT_VALUES,
    });

    const invalidatePermissions = () =>
        queryClient.invalidateQueries({
            queryKey: getAttendancePermissionsByStudentQueryKey({ studentId }),
        });

    const { mutate: createPermission, isPending: isCreating } =
        useCreateAttendancePermission({
            mutation: {
                onSuccess: async () => {
                    await invalidatePermissions();
                    toast.success(t("permissions.createSuccessToast"));
                    form.reset(DEFAULT_VALUES);
                },
                onError: handleServerError,
            },
        });

    const onSubmitPermission = (values: Values) => {
        createPermission({
            data: {
                studentId,
                fromDate: values.fromDate,
                toDate: values.toDate,
                reason: values.reason?.trim() || undefined,
            },
        });
    };

    return (
        <Form {...form}>
            <form
                onSubmit={form.handleSubmit(onSubmitPermission)}
                className="space-y-3"
            >
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                    <FormField
                        control={form.control}
                        name="fromDate"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>
                                    {t("permissions.fields.fromDate")}
                                </FormLabel>
                                <FormControl>
                                    <Input
                                        type="date"
                                        disabled={isCreating}
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="toDate"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>
                                    {t("permissions.fields.toDate")}
                                </FormLabel>
                                <FormControl>
                                    <Input
                                        type="date"
                                        disabled={isCreating}
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                </div>
                <FormField
                    control={form.control}
                    name="reason"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>
                                {t("permissions.fields.reason")}
                            </FormLabel>
                            <FormControl>
                                <Textarea disabled={isCreating} {...field} />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <Button
                    type="submit"
                    disabled={isCreating}
                    className="w-full sm:w-auto"
                >
                    {t("permissions.requestAction")}
                </Button>
            </form>
        </Form>
    );
}
