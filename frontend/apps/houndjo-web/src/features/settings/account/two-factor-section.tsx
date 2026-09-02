"use client";

import { useMemo, useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import { AxiosError } from "axios";
import {
    getUserDetailsQueryKey,
    twoFactorSetupRequestTypeEnum,
    useConfirm2FactorSetup,
    useDisable2Factor,
    useGetUserDetails,
    useInit2FactorSetup,
} from "@api-client";
import { QRCodeSVG } from "qrcode.react";
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
import { PasswordInput } from "@/components/password-input";
import { Skeleton } from "@/components/ui/skeleton";

// The generated client types this response as a bare `object` since its
// shape depends on the setup `type` (see TwoFactorController#init2FactorSetup);
// TOTP always returns these two fields.
type TotpSetupResponse = { secret: string; otpAuthUri: string };

function createConfirmSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        code: z.string().min(1, t("codeRequired")),
    });
}

function createDisableSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        currentPassword: z.string().min(1, t("passwordRequired")),
    });
}

type ConfirmFormValues = z.infer<ReturnType<typeof createConfirmSchema>>;
type DisableFormValues = z.infer<ReturnType<typeof createDisableSchema>>;

function SetupFlow() {
    const t = useTranslations("SettingsAccount.twoFactor.setup");
    const tValidation = useTranslations(
        "SettingsAccount.twoFactor.setup.validation"
    );
    const queryClient = useQueryClient();
    const [setup, setSetup] = useState<TotpSetupResponse | null>(null);

    const confirmSchema = useMemo(
        () => createConfirmSchema(tValidation),
        [tValidation]
    );
    const form = useForm<ConfirmFormValues>({
        resolver: zodResolver(confirmSchema),
        defaultValues: { code: "" },
    });

    const initSetup = useInit2FactorSetup({
        mutation: {
            onSuccess: (response) => setSetup(response as TotpSetupResponse),
            onError: handleServerError,
        },
    });

    const confirmSetup = useConfirm2FactorSetup({
        mutation: {
            onSuccess: () => {
                queryClient.invalidateQueries({
                    queryKey: getUserDetailsQueryKey(),
                });
                toast.success(t("toastEnabled"));
                setSetup(null);
                form.reset();
            },
            onError: (error) => {
                const status =
                    error instanceof AxiosError
                        ? error.response?.status
                        : undefined;

                if (status === 403 || status === 409) {
                    form.setError("code", { message: t("invalidCode") });
                    return;
                }

                handleServerError(error);
            },
        },
    });

    function onConfirmSubmit(values: ConfirmFormValues) {
        confirmSetup.mutate({ data: { code: values.code } });
    }

    if (!setup) {
        return (
            <Button
                size="sm"
                disabled={initSetup.isPending}
                onClick={() =>
                    initSetup.mutate({
                        data: { type: twoFactorSetupRequestTypeEnum.TOTP },
                    })
                }
            >
                {initSetup.isPending ? t("startingButton") : t("startButton")}
            </Button>
        );
    }

    return (
        <div className="space-y-4">
            <div className="space-y-2">
                <p className="text-sm text-muted-foreground">
                    {t("scanHint")}
                </p>
                <div className="w-fit rounded-md border bg-white p-3">
                    <QRCodeSVG value={setup.otpAuthUri} size={168} />
                </div>
                <p className="text-sm text-muted-foreground">
                    {t("manualEntryHint")}
                </p>
                <code className="block w-fit rounded-md bg-muted px-3 py-2 text-sm">
                    {setup.secret}
                </code>
            </div>
            <Form {...form}>
                <form
                    onSubmit={form.handleSubmit(onConfirmSubmit)}
                    className="flex items-end gap-2"
                >
                    <FormField
                        control={form.control}
                        name="code"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>{t("codeLabel")}</FormLabel>
                                <FormControl>
                                    <Input
                                        inputMode="numeric"
                                        autoComplete="one-time-code"
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <Button type="submit" size="sm" disabled={confirmSetup.isPending}>
                        {confirmSetup.isPending
                            ? t("confirmingButton")
                            : t("confirmButton")}
                    </Button>
                </form>
            </Form>
        </div>
    );
}

function DisableFlow() {
    const t = useTranslations("SettingsAccount.twoFactor.disable");
    const tValidation = useTranslations(
        "SettingsAccount.twoFactor.disable.validation"
    );
    const queryClient = useQueryClient();

    const disableSchema = useMemo(
        () => createDisableSchema(tValidation),
        [tValidation]
    );
    const form = useForm<DisableFormValues>({
        resolver: zodResolver(disableSchema),
        defaultValues: { currentPassword: "" },
    });

    const disable2Factor = useDisable2Factor({
        mutation: {
            onSuccess: () => {
                queryClient.invalidateQueries({
                    queryKey: getUserDetailsQueryKey(),
                });
                toast.success(t("toastDisabled"));
                form.reset();
            },
            onError: (error) => {
                const status =
                    error instanceof AxiosError
                        ? error.response?.status
                        : undefined;

                if (status === 403 || status === 409) {
                    form.setError("currentPassword", {
                        message: t("invalidPassword"),
                    });
                    return;
                }

                handleServerError(error);
            },
        },
    });

    function onSubmit(values: DisableFormValues) {
        disable2Factor.mutate({
            data: { currentPassword: values.currentPassword },
        });
    }

    return (
        <div className="space-y-4">
            <p className="text-sm text-muted-foreground">{t("description")}</p>
            <Form {...form}>
                <form
                    onSubmit={form.handleSubmit(onSubmit)}
                    className="flex items-end gap-2"
                >
                    <FormField
                        control={form.control}
                        name="currentPassword"
                        render={({ field }) => (
                            <FormItem>
                                <FormLabel>
                                    {t("currentPasswordLabel")}
                                </FormLabel>
                                <FormControl>
                                    <PasswordInput
                                        autoComplete="current-password"
                                        {...field}
                                    />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                    <Button
                        type="submit"
                        size="sm"
                        variant="destructive"
                        disabled={disable2Factor.isPending}
                    >
                        {disable2Factor.isPending
                            ? t("disablingButton")
                            : t("disableButton")}
                    </Button>
                </form>
            </Form>
        </div>
    );
}

export function TwoFactorSection() {
    const t = useTranslations("SettingsAccount.twoFactor");
    const { data: user, isLoading } = useGetUserDetails();

    return (
        <div className="space-y-4">
            <div>
                <h4 className="text-sm font-medium">{t("title")}</h4>
                <p className="mt-1 text-sm text-muted-foreground">
                    {t("description")}
                </p>
            </div>
            {isLoading ? (
                <Skeleton className="h-9 w-40" />
            ) : user?.twoFactorEnabled ? (
                <DisableFlow />
            ) : (
                <SetupFlow />
            )}
        </div>
    );
}
