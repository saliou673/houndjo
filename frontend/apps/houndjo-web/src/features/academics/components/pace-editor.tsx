"use client";

import { useEffect, useMemo, type ReactNode } from "react";
import { z } from "zod";
import { useForm, type Control } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    useGetPace,
    useSetPace,
    type CourseTypeEnumKey,
    type PaceUnitEnumKey,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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

const UNITS: PaceUnitEnumKey[] = ["PAGE", "VERSE", "HIZB", "NISF_HIZB", "LESSON", "CHAPTER"];
const FLOW_UNITS: PaceUnitEnumKey[] = ["PAGE", "VERSE", "HIZB", "NISF_HIZB"];

function createFormSchema(t: ReturnType<typeof useTranslations>) {
    return z
        .object({
            unit: z.enum(["PAGE", "VERSE", "HIZB", "NISF_HIZB", "LESSON", "CHAPTER"]),
            amountPerSession: z.number().min(0.01),
            sessionsPerWeek: z.number().int().min(1).max(7),
            sabakUnit: z
                .enum(["PAGE", "VERSE", "HIZB", "NISF_HIZB", "LESSON", "CHAPTER"])
                .optional(),
            sabakAmount: z.number().min(0.01).optional(),
            sabqiUnit: z
                .enum(["PAGE", "VERSE", "HIZB", "NISF_HIZB", "LESSON", "CHAPTER"])
                .optional(),
            sabqiAmount: z.number().min(0.01).optional(),
            dhorUnit: z
                .enum(["PAGE", "VERSE", "HIZB", "NISF_HIZB", "LESSON", "CHAPTER"])
                .optional(),
            dhorAmount: z.number().min(0.01).optional(),
            dhorCycleDays: z.number().int().min(1).optional(),
        })
        .superRefine((values, ctx) => {
            const flows: Array<
                ["sabakUnit" | "sabqiUnit" | "dhorUnit", "sabakAmount" | "sabqiAmount" | "dhorAmount"]
            > = [
                ["sabakUnit", "sabakAmount"],
                ["sabqiUnit", "sabqiAmount"],
                ["dhorUnit", "dhorAmount"],
            ];
            for (const [unitKey, amountKey] of flows) {
                if (values[unitKey] == null) {
                    ctx.addIssue({
                        code: z.ZodIssueCode.custom,
                        message: t("flowUnitRequired"),
                        path: [unitKey],
                    });
                }
                if (values[amountKey] == null) {
                    ctx.addIssue({
                        code: z.ZodIssueCode.custom,
                        message: t("flowAmountRequired"),
                        path: [amountKey],
                    });
                }
            }
            if (values.dhorCycleDays == null) {
                ctx.addIssue({
                    code: z.ZodIssueCode.custom,
                    message: t("dhorCycleDaysRequired"),
                    path: ["dhorCycleDays"],
                });
            }
        });
}

type PaceForm = z.infer<ReturnType<typeof createFormSchema>>;

type FlowFieldsProps = {
    control: Control<PaceForm>;
    t: ReturnType<typeof useTranslations>;
    title: string;
    unitField: "sabakUnit" | "sabqiUnit" | "dhorUnit";
    amountField: "sabakAmount" | "sabqiAmount" | "dhorAmount";
    disabled: boolean;
    children?: ReactNode;
};

function FlowFields({
    control,
    t,
    title,
    unitField,
    amountField,
    disabled,
    children,
}: FlowFieldsProps) {
    return (
        <div className="space-y-3 rounded-md border p-3">
            <h4 className="text-sm font-semibold">{title}</h4>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
                <FormField
                    control={control}
                    name={unitField}
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.unit")}</FormLabel>
                            <Select
                                disabled={disabled}
                                value={field.value}
                                onValueChange={field.onChange}
                            >
                                <FormControl>
                                    <SelectTrigger className="w-full">
                                        <SelectValue placeholder={t("fields.unitPlaceholder")} />
                                    </SelectTrigger>
                                </FormControl>
                                <SelectContent>
                                    {FLOW_UNITS.map((value) => (
                                        <SelectItem key={value} value={value}>
                                            {t(`unitOptions.${value}`)}
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
                    name={amountField}
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>{t("fields.amount")}</FormLabel>
                            <FormControl>
                                <Input
                                    type="number"
                                    step="0.01"
                                    min={0.01}
                                    disabled={disabled}
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
                {children}
            </div>
        </div>
    );
}

type PaceEditorProps = {
    courseId: number;
    courseType: CourseTypeEnumKey;
    canUpdate: boolean;
};

export function PaceEditor({ courseId, courseType, canUpdate }: PaceEditorProps) {
    const t = useTranslations("Classes.pace");
    const tValidation = useTranslations("Classes.pace.validation");
    const isQuran = courseType === "QURAN";
    const queryClient = useQueryClient();
    const { data: pace, isLoading } = useGetPace(courseId);

    const formSchema = useMemo(() => createFormSchema(tValidation), [tValidation]);
    const form = useForm<PaceForm>({
        resolver: isQuran ? zodResolver(formSchema) : undefined,
        defaultValues: {
            unit: "PAGE",
            amountPerSession: 1,
            sessionsPerWeek: 1,
        },
    });

    useEffect(() => {
        if (pace) {
            form.reset({
                unit: pace.unit ?? "PAGE",
                amountPerSession: pace.amountPerSession ?? 1,
                sessionsPerWeek: pace.sessionsPerWeek ?? 1,
                sabakUnit: pace.sabak?.unit,
                sabakAmount: pace.sabak?.amount,
                sabqiUnit: pace.sabqi?.unit,
                sabqiAmount: pace.sabqi?.amount,
                dhorUnit: pace.dhor?.unit,
                dhorAmount: pace.dhor?.amount,
                dhorCycleDays: pace.dhorCycleDays,
            });
        }
    }, [pace, form]);

    const { mutate: setPace, isPending } = useSetPace({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: [
                        { url: "/api/v1/courses/:courseId/pace", params: { courseId } },
                    ],
                });
                toast.success(t("toastSaved"));
            },
            onError: handleServerError,
        },
    });

    const onSubmit = (values: PaceForm) => {
        setPace({
            courseId,
            data: {
                unit: values.unit,
                amountPerSession: values.amountPerSession,
                sessionsPerWeek: values.sessionsPerWeek,
                sabak:
                    isQuran && values.sabakUnit && values.sabakAmount != null
                        ? { unit: values.sabakUnit, amount: values.sabakAmount }
                        : undefined,
                sabqi:
                    isQuran && values.sabqiUnit && values.sabqiAmount != null
                        ? { unit: values.sabqiUnit, amount: values.sabqiAmount }
                        : undefined,
                dhor:
                    isQuran && values.dhorUnit && values.dhorAmount != null
                        ? { unit: values.dhorUnit, amount: values.dhorAmount }
                        : undefined,
                dhorCycleDays: isQuran ? values.dhorCycleDays : undefined,
            },
        });
    };

    if (isLoading) {
        return <p className="text-sm text-muted-foreground">{t("loading")}</p>;
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-lg">{t("title")}</CardTitle>
            </CardHeader>
            <CardContent>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
                            <FormField
                                control={form.control}
                                name="unit"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>{t("fields.unit")}</FormLabel>
                                        <Select
                                            disabled={!canUpdate || isPending}
                                            value={field.value}
                                            onValueChange={field.onChange}
                                        >
                                            <FormControl>
                                                <SelectTrigger className="w-full">
                                                    <SelectValue />
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                {UNITS.map((value) => (
                                                    <SelectItem key={value} value={value}>
                                                        {t(`unitOptions.${value}`)}
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
                                name="amountPerSession"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>
                                            {t("fields.amountPerSession")}
                                        </FormLabel>
                                        <FormControl>
                                            <Input
                                                type="number"
                                                step="0.01"
                                                min={0.01}
                                                disabled={!canUpdate || isPending}
                                                value={field.value}
                                                onChange={(event) =>
                                                    field.onChange(Number(event.target.value))
                                                }
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="sessionsPerWeek"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>
                                            {t("fields.sessionsPerWeek")}
                                        </FormLabel>
                                        <FormControl>
                                            <Input
                                                type="number"
                                                min={1}
                                                max={7}
                                                disabled={!canUpdate || isPending}
                                                value={field.value}
                                                onChange={(event) =>
                                                    field.onChange(Number(event.target.value))
                                                }
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>

                        {isQuran && (
                            <div className="space-y-4">
                                <FlowFields
                                    control={form.control}
                                    t={t}
                                    title={t("flows.sabak")}
                                    unitField="sabakUnit"
                                    amountField="sabakAmount"
                                    disabled={!canUpdate || isPending}
                                />
                                <FlowFields
                                    control={form.control}
                                    t={t}
                                    title={t("flows.sabqi")}
                                    unitField="sabqiUnit"
                                    amountField="sabqiAmount"
                                    disabled={!canUpdate || isPending}
                                />
                                <FlowFields
                                    control={form.control}
                                    t={t}
                                    title={t("flows.dhor")}
                                    unitField="dhorUnit"
                                    amountField="dhorAmount"
                                    disabled={!canUpdate || isPending}
                                >
                                    <FormField
                                        control={form.control}
                                        name="dhorCycleDays"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>
                                                    {t("fields.dhorCycleDays")}
                                                </FormLabel>
                                                <FormControl>
                                                    <Input
                                                        type="number"
                                                        min={1}
                                                        disabled={!canUpdate || isPending}
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
                                </FlowFields>
                            </div>
                        )}

                        {canUpdate && (
                            <Button type="submit" disabled={isPending}>
                                {t("submit")}
                            </Button>
                        )}
                    </form>
                </Form>
            </CardContent>
        </Card>
    );
}
